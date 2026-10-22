// 小程序网关会话：1 路 Ingress 接收小程序推流 + 每个远端参与者 1 路 Track Composite Egress 推到 SRS
import { createHash, randomBytes, timingSafeEqual } from 'node:crypto'
import { setTimeout as delay } from 'node:timers/promises'
import { GATEWAY_SRS_APP_PATTERN } from './config.mjs'
import { LiveKitApiError } from './livekit-api.mjs'

const RESOURCE_PREFIX = 'yudao-mp-gw-' // 网关创建的 Ingress 名称前缀；单实例启动时据此回收残留
const USER_IDENTITY_PATTERN = /^[1-9]\d{0,18}$/ // 业务参与者 identity：数字 userId
const EXCLUDED_KINDS = new Set(['EGRESS', 'AGENT', 'SIP']) // 非业务参与者类型；其它小程序成员的 INGRESS 属于业务参与者
const EGRESS_LIST_GRACE_MS = 10000 // 新建 Egress 在活跃列表中可见前的宽限期
const SESSION_ID_PATTERN = /^[0-9a-f]{32}$/ // 会话编号格式
const STREAM_PREFIX = 'yudaompgw_' // 网关推往 SRS 的流名前缀
const STREAM_NAME_PATTERN = /^yudaompgw_[0-9a-f]{12}_[1-9]\d{0,18}_[0-9a-f]{16}$/ // 网关流名完整格式
const RELEASE_CONCURRENCY = 4 // 定期重试时同时进行的释放尝试上限
const SHUTDOWN_RELEASE_RETRY_MS = 500 // 退出时释放失败后的重试间隔；整体时长由入口的退出超时兜底
const REDACTED_STREAM_PATTERN = /^\{yud\.\.\.[\w-]{3}\}$/ // LiveKit 脱敏后的网关流名：首 3 位 + ... + 末 3 位（末位来自推流密钥）

/** 网关业务错误；status 为返回给客户端的 HTTP 状态码 */
export class GatewayError extends Error {
  /**
   * @param {number} status HTTP 状态码
   * @param {string} code 错误码
   * @param {string} message 错误说明
   */
  constructor(status, code, message) {
    super(message)
    this.status = status
    this.code = code
  }
}

/**
 * 计算 SHA-256 摘要；会话凭证、推拉流密钥只保存摘要用于比对
 *
 * @param {string} value 原文
 * @returns {Buffer} 摘要
 */
function digest(value) {
  return createHash('sha256').update(value).digest()
}

/**
 * 常量时间比较凭证
 *
 * @param {unknown} value 待校验原文
 * @param {Buffer} expectedDigest 期望摘要
 * @returns {boolean} 是否一致
 */
function matches(value, expectedDigest) {
  return typeof value === 'string' && value.length > 0 && timingSafeEqual(digest(value), expectedDigest)
}

/**
 * 是否为业务参与者：数字 userId、非 Egress 等内部参与者、未断开
 *
 * @param {{ identity: string, kind: string, state: string }} participant 参与者
 * @returns {boolean} 是否业务参与者
 */
function isBusinessParticipant(participant) {
  return USER_IDENTITY_PATTERN.test(participant.identity)
    && !EXCLUDED_KINDS.has(participant.kind)
    && participant.state !== 'DISCONNECTED'
}

/**
 * 选择远端参与者要转出的媒体：视频屏幕共享优先、摄像头回退；音频取麦克风
 * <p>
 * 摄像头静音不更换 Egress，只回传 videoVisible，避免反复重建推流
 *
 * @param {{ tracks: Array<{ sid: string, type: string, source: string, muted: boolean }> }} participant 参与者
 * @returns {{ audioSid?: string, videoSid?: string, videoSource?: string, videoVisible: boolean }} 媒体选择
 */
function pickMedia(participant) {
  const screenShare = participant.tracks.find(track => track.type === 'VIDEO' && track.source === 'SCREEN_SHARE' && !track.muted)
  const camera = participant.tracks.find(track => track.type === 'VIDEO' && (track.source === 'CAMERA' || track.source === 'UNKNOWN'))
  const video = screenShare || camera
  const audio = participant.tracks.find(track => track.type === 'AUDIO' && (track.source === 'MICROPHONE' || track.source === 'UNKNOWN'))
  return {
    audioSid: audio?.sid,
    videoSid: video?.sid,
    videoSource: video?.source,
    videoVisible: !!video && !video.muted,
  }
}

/**
 * 创建调用的结果是否未知：超时、网络错误、HTTP 5xx（含代理 502 / 504 与响应缺少资源编号）时，
 * 请求可能已被 LiveKit 受理、资源已建但编号丢失
 * <p>
 * 4xx 等明确拒绝的失败按未创建处理，沿用冷却重试
 *
 * @param {unknown} error 错误
 * @returns {boolean} 是否结果未知
 */
function isAmbiguousCreateError(error) {
  return error instanceof LiveKitApiError && (error.status === 0 || error.status >= 500)
}

/**
 * 解析 SRS 回调中的查询参数
 *
 * @param {unknown} param 如 "?token=xxx"
 * @returns {URLSearchParams} 参数
 */
function parseHookParam(param) {
  return new URLSearchParams(typeof param === 'string' ? param.replace(/^\?/, '') : '')
}

/**
 * 创建会话管理器（单实例）
 *
 * @param {Readonly<Record<string, any>>} config 网关配置
 * @param {ReturnType<import('./livekit-api.mjs').createLiveKitApi>} liveKit LiveKit API
 * @param {{ info: Function, warn: Function }} logger 日志；只记录编号与房间，不记录凭证
 * @returns {Record<string, Function>} 会话管理方法
 */
export function createSessionManager(config, liveKit, logger) {
  const sessions = new Map() // 会话编号 -> 会话
  const sessionsByUser = new Map() // 房间 + identity -> 会话；同一用户同一房间只允许一份会话
  const streams = new Map() // SRS 流名 -> { session, entry }；会话关闭或远端移除时立即删除，撤销推拉流授权
  const pendingReleases = new Map() // 「类型:资源编号」-> 待确认释放的 Ingress / Egress；释放成功前持续占用配额并定期重试
  const inflightCreates = new Set() // 进行中的 Ingress / Egress 创建；结果返回前占用配额，退出时等待其完成
  const unknownCreates = new Set() // 结果未知的创建（超时 / 网络错误）；资源可能已在 LiveKit 侧存在，进程内永久占用配额与用户维度，只能重启后由启动回收清理
  let shuttingDown = false // 是否正在退出
  const reaper = setInterval(reapSessions, 1000) // 租约、Token 过期扫描与释放重试

  /**
   * 会话的用户维度键
   *
   * @param {{ room: string, identity: string }} value 会话或授权
   * @returns {string} 键
   */
  function userKey(value) {
    return `${value.room}\n${value.identity}`
  }

  /**
   * 当前占用的 Ingress 数：存活会话 + 待确认释放 + 归属已失效但仍在创建中 + 结果未知的 Ingress
   *
   * @returns {number} 数量
   */
  function countIngress() {
    let count = sessions.size
    pendingReleases.forEach((item) => {
      if (item.kind === 'ingress') {
        count++
      }
    })
    inflightCreates.forEach((item) => {
      if (item.kind === 'ingress' && !item.isCurrent()) {
        count++
      }
    })
    unknownCreates.forEach((item) => {
      if (item.kind === 'ingress') {
        count++
      }
    })
    return count
  }

  /**
   * 当前占用的 Egress 路数：存活会话中的远端流（含启动中）+ 待确认释放 + 归属已失效但仍在创建中 + 结果未知的 Egress
   *
   * @returns {number} 路数
   */
  function countEgress() {
    let count = 0
    sessions.forEach((session) => {
      count += session.remotes.size
    })
    pendingReleases.forEach((item) => {
      if (item.kind === 'egress') {
        count++
      }
    })
    inflightCreates.forEach((item) => {
      if (item.kind === 'egress' && !item.isCurrent()) {
        count++
      }
    })
    unknownCreates.forEach((item) => {
      if (item.kind === 'egress') {
        count++
      }
    })
    return count
  }

  /**
   * 同一房间 + identity 是否仍有未释放的会话或推流资源（含释放中、创建中、结果未知）
   *
   * @param {string} key 用户维度键
   * @returns {boolean} 是否占用
   */
  function isUserBusy(key) {
    if (sessionsByUser.has(key)) {
      return true
    }
    for (const item of pendingReleases.values()) {
      if (item.ownerKey === key) {
        return true
      }
    }
    for (const item of inflightCreates) {
      if (item.ownerKey === key) {
        return true
      }
    }
    for (const item of unknownCreates) {
      if (item.ownerKey === key) {
        return true
      }
    }
    return false
  }

  /**
   * 尝试释放一个待释放资源；同一资源同时只有一次尝试，成功后移出待释放列表，失败则等待下次重试
   *
   * @param {{ kind: 'ingress' | 'egress', id: string, attempt?: Promise<boolean>, nextAt: number }} item 待释放资源
   * @returns {Promise<boolean>} 是否已确认释放
   */
  function attemptRelease(item) {
    if (item.attempt) {
      return item.attempt
    }
    const key = `${item.kind}:${item.id}`
    const attempt = (item.kind === 'ingress' ? liveKit.deleteIngress(item.id) : liveKit.stopEgress(item.id))
      .then(() => {
        if (pendingReleases.get(key) === item) {
          pendingReleases.delete(key)
        }
        return true
      }, (error) => {
        item.nextAt = Date.now() + config.releaseRetryMs
        logger.warn('资源释放失败，稍后重试', { kind: item.kind, id: item.id, error: error.message })
        return false
      })
      .finally(() => {
        if (item.attempt === attempt) {
          item.attempt = undefined
        }
      })
    item.attempt = attempt
    return attempt
  }

  /**
   * 登记并立即释放资源；释放确认前资源保留在待释放列表中，继续占用配额
   *
   * @param {{ kind: 'ingress' | 'egress', id: string, ownerKey?: string }} resource 资源；Ingress 带用户维度键
   * @returns {Promise<boolean>} 首次尝试是否已确认释放
   */
  function releaseResource(resource) {
    const key = `${resource.kind}:${resource.id}`
    let item = pendingReleases.get(key)
    if (!item) {
      item = { ...resource, attempt: undefined, nextAt: 0 }
      pendingReleases.set(key, item)
    }
    return attemptRelease(item)
  }

  /**
   * 重试到期的待释放资源，同时进行的释放尝试不超过上限
   *
   * @param {boolean} force 是否忽略重试间隔（退出时使用）
   */
  function retryReleases(force) {
    const now = Date.now()
    let active = 0 // 本轮扫描时正在进行的释放尝试
    pendingReleases.forEach((item) => {
      if (item.attempt) {
        active++
      }
    })
    pendingReleases.forEach((item) => {
      if (active < RELEASE_CONCURRENCY && !item.attempt && (force || item.nextAt <= now)) {
        active++
        void attemptRelease(item)
      }
    })
  }

  /**
   * 跟踪资源创建：结果返回时若归属已失效（会话关闭、远端已更换），在移出创建列表前转入待释放，配额不出现空档；
   * 结果未知（超时 / 网络错误）时转入未知列表，继续占用配额与用户维度，不再盲目重建
   *
   * @param {{ kind: 'ingress' | 'egress', ownerKey: string, isCurrent: () => boolean }} item 创建项
   * @param {Promise<Record<string, any>>} promise LiveKit 创建调用
   * @returns {Promise<Record<string, any>>} 创建结果
   */
  function trackCreate(item, promise) {
    inflightCreates.add(item)
    const task = promise.then((result) => {
      const id = item.kind === 'ingress' ? result.ingressId : result.egressId
      if (id && !item.isCurrent()) {
        void releaseResource({ kind: item.kind, id, ownerKey: item.ownerKey })
      }
      inflightCreates.delete(item)
      return result
    }, (error) => {
      if (isAmbiguousCreateError(error)) {
        unknownCreates.add(item)
        logger.warn('创建结果未知，保留占位直到网关重启后启动回收', { kind: item.kind, error: error.message })
      }
      inflightCreates.delete(item)
      throw error
    })
    item.settled = task.then(() => undefined, () => undefined)
    return task
  }

  /**
   * 创建会话：同一房间 + identity 已有任何未释放的会话或推流资源时返回 409，不替换正在推流的会话
   * <p>
   * 替换已进房的 Ingress 会触发 participant_left，后端会据此结束私聊通话，因此由原会话 DELETE 或租约到期后才允许新建
   *
   * @param {{ room: string, identity: string, name: string, exp: number }} claims 房间 Token 授权
   * @returns {Promise<{ session: Record<string, any>, sessionToken: string }>} 会话与会话凭证
   */
  async function createSession(claims) {
    if (shuttingDown) {
      throw new GatewayError(503, 'shutting_down', '网关正在退出')
    }
    if (isUserBusy(userKey(claims))) {
      throw new GatewayError(409, 'session_busy', '该用户在本房间已有小程序通话会话或资源未释放；请先结束已有会话，持续出现请联系管理员')
    }
    if (countIngress() >= config.maxSessions) {
      throw new GatewayError(429, 'session_limit', '小程序网关会话数已达上限')
    }
    const now = Date.now()
    const sessionToken = randomBytes(32).toString('base64url')
    const session = {
      id: randomBytes(16).toString('hex'), // 会话编号，同时作为资源名后缀
      tokenDigest: digest(sessionToken), // 会话凭证摘要
      room: claims.room, // 房间名，取自 Token
      identity: claims.identity, // 用户 identity，取自 Token
      status: 'starting', // starting / active / closed
      closed: false, // 是否已关闭；异步步骤完成后据此清理迟到资源
      closeReason: undefined, // 关闭原因
      releaseTask: undefined, // 资源释放任务（首次尝试完成即结束，未确认的资源转入待释放重试）
      ingressId: undefined, // 小程序推流 Ingress
      pushUrl: undefined, // 小程序推流地址（含 stream key，仅返回给创建者）
      publishing: false, // 小程序推流是否已作为参与者进房
      everPublished: false, // 是否曾经进房；之后房间消失即视为通话结束
      remotes: new Map(), // 远端 userId -> 远端流
      waitingUserIds: [], // 已进房但尚无可转出轨道的远端
      overflowUserIds: [], // 超出单会话路数上限的远端
      capacityUserIds: [], // 因全局 Egress 上限暂未转出的远端
      remoteErrors: new Map(), // 远端 userId -> 最近一次转出失败原因
      unknownUserIds: new Set(), // Egress 创建结果未知的远端；本会话内不再为其重建，避免叠加未知资源
      presentUserIds: new Set(), // 最近一次同步时仍在房间内的远端
      retryAfter: new Map(), // 远端 userId -> 允许重试的时间
      syncing: false, // 是否正在同步房间
      syncError: undefined, // 最近一次同步失败原因
      syncTimer: undefined, // 同步定时器
      leaseExpiresAt: now + config.leaseTtlMs, // 租约到期时间
      expiresAt: Math.min(claims.exp * 1000, now + config.maxSessionMs), // 会话最晚结束时间
    }
    sessions.set(session.id, session)
    sessionsByUser.set(userKey(session), session)
    try {
      const ingress = await trackCreate({
        kind: 'ingress',
        ownerKey: userKey(session),
        isCurrent: () => !session.closed,
      }, liveKit.createIngress({
        room: session.room,
        identity: session.identity,
        name: claims.name,
        resourceName: `${RESOURCE_PREFIX}${session.id}`,
      }))
      if (session.closed) { // 创建期间会话已关闭：转入待释放（与 trackCreate 的登记幂等）
        if (ingress.ingressId) {
          void releaseResource({ kind: 'ingress', id: ingress.ingressId, ownerKey: userKey(session) })
        }
        throw new GatewayError(409, 'session_closed', '会话在创建过程中已关闭')
      }
      session.ingressId = ingress.ingressId
      if (!ingress.ingressId || !ingress.url || !ingress.streamKey) {
        throw new GatewayError(502, 'ingress_url_missing', 'LiveKit 未返回 RTMP 推流地址，请检查 ingress.rtmp_base_url')
      }
      session.pushUrl = `${ingress.url}/${ingress.streamKey}`
      session.status = 'active'
    } catch (error) {
      await closeSession(session, 'create_failed')
      if (error instanceof GatewayError) {
        throw error
      }
      logger.warn('创建 Ingress 失败', { room: session.room, error: error.message })
      throw new GatewayError(502, 'livekit_unavailable', '创建小程序推流失败')
    }
    session.syncTimer = setInterval(() => void syncSession(session), config.syncIntervalMs)
    void syncSession(session)
    logger.info('会话已创建', { session: session.id, room: session.room, userId: session.identity, ingressId: session.ingressId })
    return { session, sessionToken }
  }

  /**
   * 关闭会话：立即撤销推拉流授权与会话查询，释放 Ingress、Egress；重复调用返回同一个释放任务
   * <p>
   * 释放任务在首次尝试完成后结束；未确认释放的资源保留在待释放列表中重试，并继续占用配额与用户维度
   *
   * @param {Record<string, any>} session 会话
   * @param {string} reason 关闭原因
   * @returns {Promise<void>} 释放任务
   */
  function closeSession(session, reason) {
    if (session.releaseTask) {
      return session.releaseTask
    }
    session.closed = true
    session.closeReason = reason
    session.status = 'closed'
    clearInterval(session.syncTimer)
    sessions.delete(session.id)
    if (sessionsByUser.get(userKey(session)) === session) {
      sessionsByUser.delete(userKey(session))
    }
    const entries = Array.from(session.remotes.values())
    session.remotes.clear()
    entries.forEach((entry) => {
      if (streams.get(entry.streamName)?.entry === entry) {
        streams.delete(entry.streamName)
      }
    })
    const resources = entries.filter(entry => entry.egressId).map(entry => ({ kind: 'egress', id: entry.egressId }))
    if (session.ingressId) {
      resources.push({ kind: 'ingress', id: session.ingressId, ownerKey: userKey(session) })
    }
    session.releaseTask = Promise.all(resources.map(releaseResource)).then((results) => {
      logger.info('会话已关闭', { session: session.id, room: session.room, reason, pendingRelease: results.filter(released => !released).length })
    })
    return session.releaseTask
  }

  /**
   * 移除单路远端流：立即撤销授权并释放 Egress；启动中的 Egress 由 trackCreate 在返回后转入待释放
   *
   * @param {Record<string, any>} session 会话
   * @param {Record<string, any>} entry 远端流
   */
  function removeRemoteStream(session, entry) {
    if (session.remotes.get(entry.userId) === entry) {
      session.remotes.delete(entry.userId)
    }
    if (streams.get(entry.streamName)?.entry === entry) {
      streams.delete(entry.streamName)
    }
    if (entry.egressId) {
      void releaseResource({ kind: 'egress', id: entry.egressId })
    }
  }

  /**
   * 为远端参与者启动 Egress；每次启动使用新的流名与推拉流密钥
   *
   * @param {Record<string, any>} session 会话
   * @param {string} userId 远端 userId
   * @param {ReturnType<typeof pickMedia>} media 媒体选择
   * @returns {Promise<void>}
   */
  async function startRemoteStream(session, userId, media) {
    const publishSecret = randomBytes(24).toString('base64url')
    const playToken = randomBytes(24).toString('base64url')
    const entry = {
      userId, // 远端 userId
      audioSid: media.audioSid, // 转出的音频轨道
      videoSid: media.videoSid, // 转出的视频轨道
      videoSource: media.videoSource, // 视频来源（CAMERA / SCREEN_SHARE），仅用于日志排查
      videoVisible: media.videoVisible, // 视频是否可见（摄像头静音时为 false）
      streamName: `${STREAM_PREFIX}${session.id.slice(0, 12)}_${userId}_${randomBytes(8).toString('hex')}`, // SRS 流名
      publishDigest: digest(publishSecret), // Egress 推流密钥摘要
      playToken, // 小程序拉流凭证明文，仅保存在内存中，用于在会话查询中重新下发 playUrl
      playDigest: digest(playToken), // 拉流凭证摘要
      egressId: undefined, // Egress 编号
      publishing: false, // SRS 是否正在接收推流
      everPublished: false, // SRS 是否收到过推流；启动超时只看该标记
      startedAt: Date.now(), // 启动时间
    }
    session.remotes.set(userId, entry)
    streams.set(entry.streamName, { session, entry })
    const createItem = { // Egress 创建项；结果未知时作为占位保留
      kind: 'egress',
      ownerKey: userKey(session),
      isCurrent: () => !session.closed && session.remotes.get(userId) === entry,
    }
    try {
      const egress = await trackCreate(createItem, liveKit.startTrackCompositeEgress({
        room: session.room,
        audioTrackId: media.audioSid,
        videoTrackId: media.videoSid,
        url: `${config.srsPublishBaseUrl}/${entry.streamName}?secret=${publishSecret}`,
        encoding: config.egressEncoding,
      }))
      if (session.closed || session.remotes.get(userId) !== entry) { // 启动期间远端已更换：转入待释放（与 trackCreate 的登记幂等）
        if (egress.egressId) {
          void releaseResource({ kind: 'egress', id: egress.egressId })
        }
        return
      }
      entry.egressId = egress.egressId
      session.remoteErrors.delete(userId)
      logger.info('远端转出已启动', { session: session.id, userId, egressId: egress.egressId, audio: !!media.audioSid, video: !!media.videoSid })
    } catch (error) {
      if (unknownCreates.has(createItem)) {
        session.unknownUserIds.add(userId)
      }
      if (session.closed || session.remotes.get(userId) !== entry) {
        return
      }
      removeRemoteStream(session, entry)
      if (!session.unknownUserIds.has(userId)) { // 结果未知的远端不再重试，状态由 unknownUserIds 返回 failed
        session.retryAfter.set(userId, Date.now() + config.egressRetryMs)
        session.remoteErrors.set(userId, 'egress_failed')
      }
      logger.warn('远端转出启动失败', { session: session.id, userId, error: error.message })
    }
  }

  /**
   * 按房间现状同步远端转出：新增、按媒体组合重建、回收已结束的 Egress
   *
   * @param {Record<string, any>} session 会话
   * @returns {Promise<void>}
   */
  async function syncSession(session) {
    if (session.closed || session.syncing) {
      return
    }
    session.syncing = true
    try {
      const participants = await liveKit.listParticipants(session.room)
      if (session.closed) {
        return
      }
      if (participants === null) {
        session.publishing = false
        if (session.everPublished) {
          void closeSession(session, 'room_gone')
        }
        return
      }
      session.publishing = participants.some(participant => participant.identity === session.identity
        && participant.kind === 'INGRESS'
        && participant.state !== 'DISCONNECTED'
        && participant.tracks.length > 0)
      session.everPublished = session.everPublished || session.publishing
      const desired = new Map() // 远端 userId -> 媒体选择
      const waiting = []
      const overflow = []
      participants
        .filter(participant => isBusinessParticipant(participant) && participant.identity !== session.identity)
        .sort((a, b) => a.joinedAt - b.joinedAt)
        .forEach((participant) => {
          const media = pickMedia(participant)
          if (!media.audioSid && !media.videoSid) {
            waiting.push(participant.identity)
          } else if (desired.size >= config.maxRemotesPerSession) {
            overflow.push(participant.identity)
          } else {
            desired.set(participant.identity, media)
          }
        })
      Array.from(session.remotes.values()).forEach((entry) => {
        const media = desired.get(entry.userId)
        if (!media || media.audioSid !== entry.audioSid || media.videoSid !== entry.videoSid) {
          removeRemoteStream(session, entry)
        } else {
          entry.videoVisible = media.videoVisible
        }
      })
      const now = Date.now()
      // Egress 已启动但始终没有推到 SRS（如编码管线一直拿不到可输出的媒体）：停止、冷却后重试，如实返回 failed
      Array.from(session.remotes.values()).forEach((entry) => {
        if (entry.egressId && !entry.everPublished && now - entry.startedAt > config.egressPublishTimeoutMs) {
          removeRemoteStream(session, entry)
          session.retryAfter.set(entry.userId, now + config.egressRetryMs)
          session.remoteErrors.set(entry.userId, 'egress_no_media')
          logger.warn('远端转出启动超时，未收到 SRS 推流', { session: session.id, userId: entry.userId, egressId: entry.egressId, videoSource: entry.videoSource || 'NONE', audio: !!entry.audioSid })
        }
      })
      if (Array.from(session.remotes.values()).some(entry => entry.egressId && now - entry.startedAt > EGRESS_LIST_GRACE_MS)) {
        const activeIds = new Set((await liveKit.listActiveEgress(session.room)).map(item => item.egressId))
        if (session.closed) {
          return
        }
        Array.from(session.remotes.values()).forEach((entry) => {
          if (entry.egressId && now - entry.startedAt > EGRESS_LIST_GRACE_MS && !activeIds.has(entry.egressId)) {
            removeRemoteStream(session, entry)
            session.retryAfter.set(entry.userId, now + config.egressRetryMs)
            session.remoteErrors.set(entry.userId, 'egress_ended')
          }
        })
      }
      const capacity = []
      desired.forEach((media, userId) => {
        if (session.remotes.has(userId) || session.unknownUserIds.has(userId) || (session.retryAfter.get(userId) || 0) > now) {
          return
        }
        if (shuttingDown) {
          return
        }
        if (countEgress() >= config.maxEgress) {
          capacity.push(userId)
          return
        }
        void startRemoteStream(session, userId, media)
      })
      Array.from(session.retryAfter.keys()).forEach((userId) => {
        if (!desired.has(userId)) {
          session.retryAfter.delete(userId)
          session.remoteErrors.delete(userId)
        }
      })
      session.presentUserIds = new Set([...desired.keys(), ...waiting, ...overflow])
      session.waitingUserIds = waiting
      session.overflowUserIds = overflow
      session.capacityUserIds = capacity
      session.syncError = undefined
    } catch (error) {
      session.syncError = 'livekit_unavailable'
      logger.warn('同步房间失败', { session: session.id, room: session.room, error: error.message })
    } finally {
      session.syncing = false
    }
  }

  /** 扫描租约与 Token 过期的会话，并重试到期的资源释放 */
  function reapSessions() {
    retryReleases(false)
    const now = Date.now()
    sessions.forEach((session) => {
      if (now > session.leaseExpiresAt) {
        void closeSession(session, 'lease_expired')
      } else if (now > session.expiresAt) {
        void closeSession(session, 'token_expired')
      }
    })
  }

  /**
   * 按会话编号与会话凭证查找会话；任何不匹配都按不存在处理
   *
   * @param {string} sessionId 会话编号
   * @param {string | undefined} sessionToken 会话凭证
   * @returns {Record<string, any>} 会话
   */
  function authorize(sessionId, sessionToken) {
    const session = SESSION_ID_PATTERN.test(sessionId) ? sessions.get(sessionId) : undefined
    if (!session || !matches(sessionToken, session.tokenDigest)) {
      throw new GatewayError(404, 'session_not_found', '会话不存在或已结束')
    }
    return session
  }

  /**
   * 续租会话并返回小程序可见状态
   *
   * @param {Record<string, any>} session 会话
   * @returns {Record<string, any>} 会话状态
   */
  function touch(session) {
    session.leaseExpiresAt = Date.now() + config.leaseTtlMs
    const remotes = Array.from(session.remotes.values()).map(entry => ({
      userId: Number(entry.userId),
      state: entry.publishing ? 'ready' : 'starting',
      ready: entry.publishing,
      playUrl: entry.publishing ? `${config.srsPlayBaseUrl}/${entry.streamName}?token=${entry.playToken}` : null, // SRS 收到推流后才下发，避免播放器提前拉空流
      hasAudio: !!entry.audioSid,
      hasVideo: !!entry.videoSid,
      videoVisible: entry.videoVisible,
    }))
    const listed = new Set(session.remotes.keys()) // 已输出的远端，避免同一远端重复出现
    const appendState = (userIds, state) => userIds.forEach((userId) => {
      if (!listed.has(userId)) {
        listed.add(userId)
        remotes.push({ userId: Number(userId), state, ready: false, playUrl: null, hasAudio: false, hasVideo: false, videoVisible: false })
      }
    })
    appendState(session.waitingUserIds, 'waiting')
    appendState(session.overflowUserIds, 'overflow')
    appendState(session.capacityUserIds, 'capacity')
    appendState(Array.from(session.remoteErrors.keys()), 'failed')
    appendState(Array.from(session.unknownUserIds).filter(userId => session.presentUserIds.has(userId)), 'failed')
    return {
      sessionId: session.id,
      status: session.status,
      publishing: session.publishing,
      remotes,
      error: session.syncError || null,
      leaseTtlMs: config.leaseTtlMs,
      pollIntervalMs: config.pollIntervalMs,
      expiresAt: session.expiresAt,
    }
  }

  /**
   * 查找 SRS 回调对应的远端流
   *
   * @param {Record<string, any>} hook SRS 回调体
   * @returns {{ session: Record<string, any>, entry: Record<string, any> } | undefined} 远端流
   */
  function findStream(hook) {
    if (!hook || hook.app !== config.srsApp || typeof hook.stream !== 'string') {
      return undefined
    }
    const ref = streams.get(hook.stream)
    if (!ref || ref.session.closed || ref.session.remotes.get(ref.entry.userId) !== ref.entry) {
      return undefined
    }
    return ref
  }

  /**
   * SRS on_publish：只允许网关启动的 Egress 携带对应密钥推流
   *
   * @param {Record<string, any>} hook SRS 回调体
   * @returns {boolean} 是否允许
   */
  function authorizePublish(hook) {
    const ref = findStream(hook)
    if (!ref || !matches(parseHookParam(hook.param).get('secret'), ref.entry.publishDigest)) {
      return false
    }
    ref.entry.publishing = true
    ref.entry.everPublished = true
    return true
  }

  /**
   * SRS on_unpublish：标记远端流未就绪
   *
   * @param {Record<string, any>} hook SRS 回调体
   */
  function handleUnpublish(hook) {
    const ref = findStream(hook)
    if (ref && matches(parseHookParam(hook.param).get('secret'), ref.entry.publishDigest)) {
      ref.entry.publishing = false
    }
  }

  /**
   * SRS on_play：只允许持有本会话拉流凭证的小程序播放对应远端流
   *
   * @param {Record<string, any>} hook SRS 回调体
   * @returns {boolean} 是否允许
   */
  function authorizePlay(hook) {
    const ref = findStream(hook)
    return !!ref && matches(parseHookParam(hook.param).get('token'), ref.entry.playDigest)
  }

  /**
   * 判断 Egress 是否推往本网关的 SRS 流
   * <p>
   * LiveKit 会把 RTMP 地址最后一段脱敏为「{首 3 位...末 3 位}」，只有 scheme / host / port / app 可信；
   * 因此归属以网关专用 app 为准：地址需为「SRS 推流根地址 / 单段流名」，且单段为完整网关流名或其脱敏形式
   *
   * @param {unknown} value EgressInfo 原始 JSON
   * @returns {boolean} 是否本网关创建
   */
  function isGatewayEgress(value) {
    if (typeof value === 'string') {
      const prefix = `${config.srsPublishBaseUrl}/`
      if (!GATEWAY_SRS_APP_PATTERN.test(config.srsApp) || !value.startsWith(prefix)) {
        return false
      }
      const segment = value.slice(prefix.length)
      return STREAM_NAME_PATTERN.test(segment.split('?')[0]) || REDACTED_STREAM_PATTERN.test(segment)
    }
    if (value && typeof value === 'object') {
      return Object.values(value).some(isGatewayEgress)
    }
    return false
  }

  /**
   * 单实例启动时回收上次异常退出残留的 Ingress / Egress；任一资源释放失败都抛出，由调用方重试
   *
   * @returns {Promise<void>}
   */
  async function cleanupOrphans() {
    const ingresses = (await liveKit.listIngress()).filter(item => item.name.startsWith(RESOURCE_PREFIX))
    const egresses = (await liveKit.listActiveEgress()).filter(item => isGatewayEgress(item.raw))
    const results = await Promise.allSettled([
      ...ingresses.map(item => liveKit.deleteIngress(item.ingressId)),
      ...egresses.map(item => liveKit.stopEgress(item.egressId)),
    ])
    const failed = results.filter(result => result.status === 'rejected').length
    if (failed > 0) {
      throw new Error(`残留资源回收失败 ${failed} 项`)
    }
    logger.info('残留资源回收完成', { ingress: ingresses.length, egress: egresses.length })
  }

  /**
   * 退出：拒绝新会话，关闭全部会话，等待进行中的创建返回并确认全部资源释放
   * <p>
   * 释放持续失败时不会返回，由入口的退出超时以非零状态结束；存在结果未知的创建时抛出错误，同样以非零状态退出，避免误报干净退出
   *
   * @returns {Promise<void>}
   */
  async function shutdown() {
    shuttingDown = true
    clearInterval(reaper)
    Array.from(sessions.values()).forEach(session => void closeSession(session, 'shutdown'))
    while (inflightCreates.size > 0 || pendingReleases.size > 0) {
      if (inflightCreates.size > 0) {
        await Promise.allSettled(Array.from(inflightCreates, item => item.settled))
        continue
      }
      retryReleases(true)
      await Promise.allSettled(Array.from(pendingReleases.values(), item => item.attempt))
      if (pendingReleases.size > 0) {
        await delay(SHUTDOWN_RELEASE_RETRY_MS)
      }
    }
    if (unknownCreates.size > 0) {
      throw new Error(`存在 ${unknownCreates.size} 个结果未知的 Ingress / Egress 创建，需重启网关由启动回收清理`)
    }
  }

  return {
    createSession,
    closeSession,
    authorize,
    touch,
    authorizePublish,
    handleUnpublish,
    authorizePlay,
    cleanupOrphans,
    shutdown,
  }
}
