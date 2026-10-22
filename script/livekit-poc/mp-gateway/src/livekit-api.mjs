// LiveKit Server API（Twirp JSON）最小客户端：只调用网关需要的 Room / Ingress / Egress 接口
import { signAdminToken } from './jwt.mjs'

const PARTICIPANT_KINDS = ['STANDARD', 'INGRESS', 'EGRESS', 'SIP', 'AGENT'] // ParticipantInfo.Kind 枚举
const PARTICIPANT_STATES = ['JOINING', 'JOINED', 'ACTIVE', 'DISCONNECTED'] // ParticipantInfo.State 枚举
const TRACK_TYPES = ['AUDIO', 'VIDEO', 'DATA'] // TrackType 枚举
const TRACK_SOURCES = ['UNKNOWN', 'CAMERA', 'MICROPHONE', 'SCREEN_SHARE', 'SCREEN_SHARE_AUDIO'] // TrackSource 枚举
const TWIRP_CODE_PATTERN = /^[a-z_]{1,32}$/ // Twirp 错误码格式，如 not_found；不符合时按 unknown 处理

/** LiveKit API 调用失败；message 只含方法、HTTP 状态与错误码，不含上游原文、Token 或推流地址 */
export class LiveKitApiError extends Error {
  /**
   * @param {number} status HTTP 状态码
   * @param {string} code Twirp 错误码
   * @param {string} message 错误说明
   */
  constructor(status, code, message) {
    super(message)
    this.status = status
    this.code = code
  }
}

/**
 * 读取 protobuf JSON 字段；兼容 snake_case 与 lowerCamelCase 输出
 *
 * @param {Record<string, any>} value 对象
 * @param {string} snakeName snake_case 字段名
 * @returns {any} 字段值
 */
function field(value, snakeName) {
  if (value[snakeName] !== undefined) {
    return value[snakeName]
  }
  return value[snakeName.replace(/_([a-z])/g, (_, char) => char.toUpperCase())]
}

/**
 * 读取枚举名；兼容名称与数字输出，缺省值按 protobuf 语义取第 0 项
 *
 * @param {string | number | undefined} value 枚举值
 * @param {string[]} names 枚举名称表
 * @returns {string} 枚举名
 */
function enumName(value, names) {
  if (typeof value === 'number') {
    return names[value] ?? String(value)
  }
  return value || names[0]
}

/**
 * 标准化房间参与者
 *
 * @param {Record<string, any>} participant ParticipantInfo
 * @returns {{ sid: string, identity: string, kind: string, state: string, joinedAt: number, tracks: Array<{ sid: string, type: string, source: string, muted: boolean }> }} 参与者
 */
function normalizeParticipant(participant) {
  return {
    sid: participant.sid,
    identity: participant.identity || '',
    kind: enumName(participant.kind, PARTICIPANT_KINDS),
    state: enumName(participant.state, PARTICIPANT_STATES),
    joinedAt: Number(field(participant, 'joined_at') || 0),
    tracks: (participant.tracks || []).map(track => ({
      sid: track.sid,
      type: enumName(track.type, TRACK_TYPES),
      source: enumName(track.source, TRACK_SOURCES),
      muted: track.muted === true,
    })),
  }
}

/**
 * 创建 LiveKit API 客户端；服务地址只来自部署配置，不接受客户端传入
 *
 * @param {{ livekitHttpUrl: string, apiKey: string, apiSecret: string, requestTimeoutMs: number }} config 网关配置
 * @returns {Record<string, Function>} API 方法
 */
export function createLiveKitApi(config) {
  /**
   * 调用 Twirp 接口
   *
   * @param {string} service 服务名，如 RoomService
   * @param {string} method 方法名
   * @param {Record<string, any>} body 请求体
   * @param {Record<string, any>} video 管理 Token 授权
   * @returns {Promise<Record<string, any>>} 响应体
   */
  async function call(service, method, body, video) {
    let response
    let text
    try {
      response = await fetch(`${config.livekitHttpUrl}/twirp/livekit.${service}/${method}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${signAdminToken(config, video)}`,
        },
        body: JSON.stringify(body),
        signal: AbortSignal.timeout(config.requestTimeoutMs),
      })
      text = await response.text()
    } catch (error) {
      // 网络错误 / 超时只保留类型，不透传底层信息
      const code = error?.name === 'TimeoutError' ? 'timeout' : 'network_error'
      throw new LiveKitApiError(0, code, `${service}.${method} 请求失败：${code}`)
    }
    let data = {}
    try {
      data = text ? JSON.parse(text) : {}
    } catch {
      data = {}
    }
    if (!data || typeof data !== 'object' || Array.isArray(data)) { // null、数组、数字等非对象 JSON 视为空响应，交由状态码与资源编号校验处理
      data = {}
    }
    if (!response.ok) {
      // 上游 msg 可能回显请求中的 RTMP 地址等凭证，只保留方法、HTTP 状态与规范化的 Twirp 错误码
      const code = typeof data.code === 'string' && TWIRP_CODE_PATTERN.test(data.code) ? data.code : 'unknown'
      throw new LiveKitApiError(response.status, code, `${service}.${method} 失败：HTTP ${response.status} ${code}`)
    }
    return data
  }

  /**
   * 判断是否为「资源不存在 / 已结束」类错误，释放资源时可忽略
   *
   * @param {unknown} error 错误
   * @returns {boolean} 是否可忽略
   */
  function isGone(error) {
    return error instanceof LiveKitApiError
      && (error.status === 404 || error.code === 'not_found' || error.code === 'failed_precondition')
  }

  return {
    /**
     * 查询房间参与者
     *
     * @param {string} room 房间名
     * @returns {Promise<ReturnType<typeof normalizeParticipant>[] | null>} 参与者；房间不存在返回 null
     */
    async listParticipants(room) {
      try {
        const data = await call('RoomService', 'ListParticipants', { room }, { roomAdmin: true, room })
        return (data.participants || []).map(normalizeParticipant)
      } catch (error) {
        if (error instanceof LiveKitApiError && (error.status === 404 || error.code === 'not_found')) {
          return null
        }
        throw error
      }
    },

    /**
     * 创建 RTMP Ingress；参与者身份直接使用 Token 内的 userId，保留同 identity 互踢语义
     *
     * @param {{ room: string, identity: string, name: string, resourceName: string }} options 参数
     * @returns {Promise<{ ingressId: string, url: string, streamKey: string }>} Ingress 信息
     */
    async createIngress(options) {
      const data = await call('Ingress', 'CreateIngress', {
        input_type: 'RTMP_INPUT',
        name: options.resourceName,
        room_name: options.room,
        participant_identity: options.identity,
        participant_name: options.name,
        enable_transcoding: true,
      }, { ingressAdmin: true })
      const ingressId = field(data, 'ingress_id')
      if (typeof ingressId !== 'string' || !ingressId) { // 可能已创建但拿不到编号，交由调用方按结果未知处理
        throw new LiveKitApiError(502, 'invalid_response', 'Ingress.CreateIngress 响应缺少资源编号')
      }
      return {
        ingressId,
        url: data.url || '',
        streamKey: field(data, 'stream_key') || '',
      }
    },

    /**
     * 删除 Ingress；不存在时忽略
     *
     * @param {string} ingressId Ingress 编号
     * @returns {Promise<void>}
     */
    async deleteIngress(ingressId) {
      try {
        await call('Ingress', 'DeleteIngress', { ingress_id: ingressId }, { ingressAdmin: true })
      } catch (error) {
        if (!isGone(error)) {
          throw error
        }
      }
    },

    /**
     * 列出全部 Ingress，用于启动时回收本网关残留资源
     *
     * @returns {Promise<Array<{ ingressId: string, name: string }>>} Ingress 列表
     */
    async listIngress() {
      const data = await call('Ingress', 'ListIngress', {}, { ingressAdmin: true })
      return (data.items || []).map(item => ({ ingressId: field(item, 'ingress_id'), name: item.name || '' }))
    },

    /**
     * 启动单个远端参与者的 Track Composite Egress，推 RTMP 到 SRS
     *
     * @param {{ room: string, audioTrackId?: string, videoTrackId?: string, url: string, encoding: Record<string, number> }} options 参数
     * @returns {Promise<{ egressId: string }>} Egress 信息
     */
    async startTrackCompositeEgress(options) {
      const data = await call('Egress', 'StartTrackCompositeEgress', {
        room_name: options.room,
        audio_track_id: options.audioTrackId || '',
        video_track_id: options.videoTrackId || '',
        stream_outputs: [{ protocol: 'RTMP', urls: [options.url] }],
        advanced: {
          width: options.encoding.width,
          height: options.encoding.height,
          framerate: options.encoding.framerate,
          audio_codec: 'AAC',
          audio_bitrate: options.encoding.audioBitrate,
          video_codec: 'H264_BASELINE',
          video_bitrate: options.encoding.videoBitrate,
          key_frame_interval: options.encoding.keyFrameInterval,
        },
      }, { roomRecord: true })
      const egressId = field(data, 'egress_id')
      if (typeof egressId !== 'string' || !egressId) { // 可能已创建但拿不到编号，交由调用方按结果未知处理
        throw new LiveKitApiError(502, 'invalid_response', 'Egress.StartTrackCompositeEgress 响应缺少资源编号')
      }
      return { egressId }
    },

    /**
     * 停止 Egress；已结束或不存在时忽略
     *
     * @param {string} egressId Egress 编号
     * @returns {Promise<void>}
     */
    async stopEgress(egressId) {
      try {
        await call('Egress', 'StopEgress', { egress_id: egressId }, { roomRecord: true })
      } catch (error) {
        if (!isGone(error)) {
          throw error
        }
      }
    },

    /**
     * 列出活跃 Egress
     *
     * @param {string | undefined} room 房间名；为空时列出全部
     * @returns {Promise<Array<{ egressId: string, raw: Record<string, any> }>>} 活跃 Egress
     */
    async listActiveEgress(room) {
      const data = await call('Egress', 'ListEgress', room ? { room_name: room, active: true } : { active: true }, { roomRecord: true })
      return (data.items || []).map(item => ({ egressId: field(item, 'egress_id'), raw: item }))
    },
  }
}
