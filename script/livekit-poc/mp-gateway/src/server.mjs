// 小程序 RTMP 网关入口：对小程序提供会话 API，对 SRS 提供推拉流鉴权回调（两个端口分开监听）
import { createServer } from 'node:http'
import { loadConfig } from './config.mjs'
import { TokenError, verifyRoomToken } from './jwt.mjs'
import { createLiveKitApi } from './livekit-api.mjs'
import { GatewayError, createSessionManager } from './session-manager.mjs'

const MAX_BODY_BYTES = 4096 // 请求体上限；会话 API 与 SRS 回调都只有很小的 JSON
const SHUTDOWN_TIMEOUT_MS = 15000 // 退出时等待资源释放的最长时间
const SESSION_PATH_PATTERN = /^\/mp-rtc\/sessions\/([^/]+)$/ // 单个会话路径

let orphansCleaned = false // 启动回收是否已完成；完成前拒绝创建会话，避免旧残留与新会话并存

/** 日志：只输出事件与编号，调用方不得传入 Token、stream key、带凭证的 URL */
const logger = {
  /**
   * @param {string} message 事件
   * @param {Record<string, unknown>} [fields] 附加字段
   */
  info(message, fields = {}) {
    console.log(JSON.stringify({ time: new Date().toISOString(), level: 'info', message, ...fields }))
  },
  /**
   * @param {string} message 事件
   * @param {Record<string, unknown>} [fields] 附加字段
   */
  warn(message, fields = {}) {
    console.warn(JSON.stringify({ time: new Date().toISOString(), level: 'warn', message, ...fields }))
  },
}

/**
 * 写 JSON 响应
 *
 * @param {import('node:http').ServerResponse} res 响应
 * @param {number} status 状态码
 * @param {unknown} [body] 响应体；为空时不写 body
 */
function sendJson(res, status, body) {
  if (res.headersSent) {
    return
  }
  const headers = { 'Cache-Control': 'no-store' }
  if (body === undefined) {
    res.writeHead(status, headers).end()
    return
  }
  res.writeHead(status, { ...headers, 'Content-Type': 'application/json; charset=utf-8' }).end(JSON.stringify(body))
}

/**
 * 读取 JSON 请求体
 *
 * @param {import('node:http').IncomingMessage} req 请求
 * @returns {Promise<Record<string, any>>} 请求体；空 body 返回空对象
 */
async function readJson(req) {
  const chunks = []
  let size = 0
  for await (const chunk of req) {
    size += chunk.length
    if (size > MAX_BODY_BYTES) {
      throw new GatewayError(413, 'body_too_large', '请求体过大')
    }
    chunks.push(chunk)
  }
  if (size === 0) {
    return {}
  }
  try {
    const value = JSON.parse(Buffer.concat(chunks).toString('utf8'))
    return value && typeof value === 'object' ? value : {}
  } catch {
    throw new GatewayError(400, 'invalid_json', '请求体不是合法 JSON')
  }
}

/**
 * 读取 Bearer 凭证
 *
 * @param {import('node:http').IncomingMessage} req 请求
 * @returns {string | undefined} 凭证
 */
function readBearer(req) {
  const header = req.headers.authorization
  return typeof header === 'string' && header.startsWith('Bearer ') ? header.slice(7).trim() : undefined
}

/**
 * 错误转响应；未知错误不回显细节
 *
 * @param {import('node:http').ServerResponse} res 响应
 * @param {unknown} error 错误
 */
function sendError(res, error) {
  if (error instanceof GatewayError || error instanceof TokenError) {
    sendJson(res, error.status, { code: error.code, message: error.message })
    return
  }
  logger.warn('请求处理失败', { error: error instanceof Error ? error.message : String(error) })
  sendJson(res, 500, { code: 'internal_error', message: '网关内部错误' })
}

/**
 * 处理小程序会话 API
 *
 * @param {Readonly<Record<string, any>>} config 网关配置
 * @param {ReturnType<typeof createSessionManager>} manager 会话管理器
 * @param {import('node:http').IncomingMessage} req 请求
 * @param {import('node:http').ServerResponse} res 响应
 * @returns {Promise<void>}
 */
async function handleApi(config, manager, req, res) {
  const { pathname } = new URL(req.url || '/', 'http://gateway')
  if (req.method === 'GET' && pathname === '/healthz') {
    sendJson(res, orphansCleaned ? 200 : 503, { status: orphansCleaned ? 'ok' : 'cleanup_pending' })
    return
  }
  if (req.method === 'POST' && pathname === '/mp-rtc/sessions') {
    if (!orphansCleaned) {
      throw new GatewayError(503, 'cleanup_pending', '网关正在回收上次残留的推流资源，请稍后再试')
    }
    await readJson(req)
    const claims = verifyRoomToken(readBearer(req), config)
    const { session, sessionToken } = await manager.createSession(claims)
    // 小程序在创建过程中断开（页面关闭、超时）：响应无人接收，立即释放刚创建的资源
    if (res.destroyed || !res.socket || res.socket.destroyed) {
      await manager.closeSession(session, 'client_aborted')
      return
    }
    sendJson(res, 201, {
      sessionId: session.id,
      sessionToken,
      userId: Number(session.identity),
      pushUrl: session.pushUrl,
      pollIntervalMs: config.pollIntervalMs,
      leaseTtlMs: config.leaseTtlMs,
      expiresAt: session.expiresAt,
    })
    return
  }
  const matched = SESSION_PATH_PATTERN.exec(pathname)
  if (matched && (req.method === 'GET' || req.method === 'DELETE')) {
    const session = manager.authorize(matched[1], readBearer(req))
    if (req.method === 'GET') {
      sendJson(res, 200, manager.touch(session))
      return
    }
    await manager.closeSession(session, 'client_closed')
    sendJson(res, 204)
    return
  }
  sendJson(res, 404, { code: 'not_found', message: '接口不存在' })
}

/**
 * 处理 SRS HTTP 回调；返回 code 0 允许，其它拒绝
 *
 * @param {ReturnType<typeof createSessionManager>} manager 会话管理器
 * @param {import('node:http').IncomingMessage} req 请求
 * @param {import('node:http').ServerResponse} res 响应
 * @returns {Promise<void>}
 */
async function handleHook(manager, req, res) {
  const { pathname } = new URL(req.url || '/', 'http://gateway')
  if (req.method !== 'POST') {
    sendJson(res, 404, { code: 404 })
    return
  }
  const hook = await readJson(req)
  let allowed = false
  if (pathname === '/srs/hooks/on_publish') {
    allowed = manager.authorizePublish(hook)
  } else if (pathname === '/srs/hooks/on_play') {
    allowed = manager.authorizePlay(hook)
  } else if (pathname === '/srs/hooks/on_unpublish') {
    manager.handleUnpublish(hook)
    allowed = true
  } else {
    sendJson(res, 404, { code: 404 })
    return
  }
  if (!allowed) {
    logger.warn('SRS 鉴权拒绝', { action: pathname.slice('/srs/hooks/'.length) })
  }
  sendJson(res, allowed ? 200 : 403, { code: allowed ? 0 : 403 })
}

/**
 * 启动 HTTP 服务
 *
 * @param {import('node:http').Server} server 服务
 * @param {string} host 监听地址
 * @param {number} port 端口
 * @returns {Promise<void>}
 */
function listen(server, host, port) {
  return new Promise((resolve, reject) => {
    server.once('error', reject)
    server.listen(port, host, () => {
      server.off('error', reject)
      resolve()
    })
  })
}

/** 启动网关 */
async function main() {
  const config = loadConfig()
  const liveKit = createLiveKitApi(config)
  const manager = createSessionManager(config, liveKit, logger)
  let stopping = false // 是否已开始退出
  let cleanupTimer // 启动回收重试定时器
  let cleanupTask // 正在执行的启动回收；退出时等待其结束

  /** 发起一次启动回收；退出后不再发起 */
  function scheduleCleanup() {
    cleanupTimer = undefined
    if (stopping) {
      return
    }
    cleanupTask = runCleanup().finally(() => {
      cleanupTask = undefined
    })
  }

  /** 启动回收：失败后按间隔重试，直到成功或网关退出 */
  async function runCleanup() {
    try {
      await manager.cleanupOrphans()
      orphansCleaned = true
    } catch (error) {
      logger.warn(stopping ? '残留资源回收失败，网关退出中不再重试' : '残留资源回收失败，稍后重试；完成前拒绝创建会话',
        { error: error.message })
      if (!stopping) {
        cleanupTimer = setTimeout(scheduleCleanup, config.cleanupRetryMs)
      }
    }
  }

  const apiServer = createServer((req, res) => {
    handleApi(config, manager, req, res).catch(error => sendError(res, error))
  })
  const hookServer = createServer((req, res) => {
    handleHook(manager, req, res).catch(error => sendError(res, error))
  })
  apiServer.requestTimeout = config.requestTimeoutMs * 3
  hookServer.requestTimeout = config.requestTimeoutMs
  await listen(apiServer, config.apiHost, config.apiPort)
  await listen(hookServer, config.hookHost, config.hookPort)
  scheduleCleanup()
  logger.info('小程序网关已启动（试验功能）', {
    api: `${config.apiHost}:${config.apiPort}`,
    hook: `${config.hookHost}:${config.hookPort}`,
    livekit: config.livekitHttpUrl,
    maxSessions: config.maxSessions,
    maxEgress: config.maxEgress,
  })

  /**
   * 退出：停止接收请求并释放全部 Ingress / Egress
   *
   * @param {string} signal 信号名
   */
  const stop = (signal) => {
    if (stopping) {
      return
    }
    stopping = true
    clearTimeout(cleanupTimer)
    logger.info('网关退出中', { signal })
    apiServer.close()
    const timer = setTimeout(() => process.exit(1), SHUTDOWN_TIMEOUT_MS)
    // 先等待进行中的启动回收（runCleanup 自身不会拒绝），再释放会话资源；启动回收未完成时残留资源未确认，不能报告正常退出
    Promise.resolve(cleanupTask).then(() => manager.shutdown()).then(() => {
      if (!orphansCleaned) {
        logger.warn('网关退出时启动残留资源回收未完成')
        return 1
      }
      return 0
    }, (error) => {
      logger.warn('网关退出时仍有资源未确认释放', { error: error.message })
      return 1
    }).then((code) => {
      clearTimeout(timer)
      hookServer.close()
      process.exit(code)
    })
  }
  process.on('SIGINT', () => stop('SIGINT'))
  process.on('SIGTERM', () => stop('SIGTERM'))
}

main().catch((error) => {
  logger.warn('网关启动失败', { error: error.message })
  process.exit(1)
})
