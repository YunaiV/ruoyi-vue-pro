// 小程序 RTMP 网关配置：全部来自环境变量；密钥只保存在内存中，不打印、不回显

/**
 * 网关专用 SRS app 命名空间
 * <p>
 * LiveKit 返回的 EgressInfo 会把 RTMP 地址最后一段（流名与查询参数）脱敏，只剩 scheme / host / port / app 可信，
 * 启动回收据此识别本网关的残留 Egress，因此该 app 只能给网关使用
 */
export const GATEWAY_SRS_APP_PATTERN = /^yudao-mp-gw(?:-[\w-]+)?$/

/**
 * 读取整数配置
 *
 * @param {Record<string, string | undefined>} env 环境变量
 * @param {string} name 变量名
 * @param {number} fallback 默认值
 * @param {number} min 最小值
 * @param {number} max 最大值
 * @returns {number} 配置值
 */
function readInt(env, name, fallback, min, max) {
  const raw = env[name]
  if (raw === undefined || raw === '') {
    return fallback
  }
  const value = Number(raw)
  if (!Number.isInteger(value) || value < min || value > max) {
    throw new Error(`${name} 需为 ${min}~${max} 的整数`)
  }
  return value
}

/**
 * 读取 URL 配置并校验协议
 *
 * @param {Record<string, string | undefined>} env 环境变量
 * @param {string} name 变量名
 * @param {string} fallback 默认值
 * @param {string[]} protocols 允许的协议，如 ['http:']
 * @returns {string} 去掉末尾斜杠的 URL
 */
function readUrl(env, name, fallback, protocols) {
  const raw = env[name] || fallback
  let url
  try {
    url = new URL(raw)
  } catch {
    throw new Error(`${name} 不是合法 URL`)
  }
  if (!protocols.includes(url.protocol) || url.search || url.hash || url.username || url.password) {
    throw new Error(`${name} 仅支持 ${protocols.join('/')} 且不能带查询参数或账号`)
  }
  return raw.replace(/\/+$/, '')
}

/**
 * 读取必填字符串配置
 *
 * @param {Record<string, string | undefined>} env 环境变量
 * @param {string} name 变量名
 * @param {number} minLength 最小长度
 * @returns {string} 配置值
 */
function readRequired(env, name, minLength) {
  const value = env[name]
  if (!value || value.length < minLength) {
    throw new Error(`${name} 未配置或长度不足 ${minLength}`)
  }
  return value
}

/**
 * 加载网关配置；未显式开启时拒绝启动
 *
 * @param {Record<string, string | undefined>} env 环境变量
 * @returns {Readonly<Record<string, any>>} 只读配置
 */
export function loadConfig(env = process.env) {
  if (env.MP_GATEWAY_ENABLED !== 'true') {
    throw new Error('MP_GATEWAY_ENABLED 未设置为 true：小程序网关为默认关闭的试验功能')
  }
  const srsPublishBaseUrl = readUrl(env, 'MP_GATEWAY_SRS_PUBLISH_BASE_URL', 'rtmp://127.0.0.1:1936/yudao-mp-gw', ['rtmp:'])
  const srsPlayBaseUrl = readUrl(env, 'MP_GATEWAY_SRS_PLAY_BASE_URL', 'rtmp://127.0.0.1:1936/yudao-mp-gw', ['rtmp:'])
  const srsApp = new URL(srsPublishBaseUrl).pathname.replace(/^\/+/, '')
  if (!GATEWAY_SRS_APP_PATTERN.test(srsApp) || new URL(srsPlayBaseUrl).pathname.replace(/^\/+/, '') !== srsApp) {
    throw new Error('MP_GATEWAY_SRS_PUBLISH_BASE_URL 与 MP_GATEWAY_SRS_PLAY_BASE_URL 需使用同一个网关专用 app（yudao-mp-gw 或 yudao-mp-gw-<后缀>）')
  }
  const pollIntervalMs = readInt(env, 'MP_GATEWAY_POLL_INTERVAL_MS', 2000, 500, 10000)
  return Object.freeze({
    apiHost: env.MP_GATEWAY_API_HOST || '127.0.0.1', // 小程序 API 监听地址；公网部署放在 HTTPS 反向代理之后
    apiPort: readInt(env, 'MP_GATEWAY_API_PORT', 8090, 1, 65535), // 小程序 API 端口
    hookHost: env.MP_GATEWAY_HOOK_HOST || '127.0.0.1', // SRS 回调监听地址，只允许 SRS 访问
    hookPort: readInt(env, 'MP_GATEWAY_HOOK_PORT', 8091, 1, 65535), // SRS 回调端口
    livekitHttpUrl: readUrl(env, 'MP_GATEWAY_LIVEKIT_HTTP_URL', 'http://127.0.0.1:7880', ['http:', 'https:']), // LiveKit Server API 地址，只取自部署配置
    apiKey: readRequired(env, 'MP_GATEWAY_LIVEKIT_API_KEY', 1), // 与后端签发房间 Token 的 apiKey 一致
    apiSecret: readRequired(env, 'MP_GATEWAY_LIVEKIT_API_SECRET', 32), // 与后端签发房间 Token 的 apiSecret 一致
    srsPublishBaseUrl, // Egress 推流到 SRS 的内部地址
    srsPlayBaseUrl, // 小程序 live-player 拉流的外部地址
    srsApp, // SRS app 名，回调时校验
    pollIntervalMs, // 建议小程序轮询间隔
    syncIntervalMs: pollIntervalMs, // 网关同步房间成员的间隔
    leaseTtlMs: readInt(env, 'MP_GATEWAY_LEASE_TTL_MS', 20000, pollIntervalMs * 3, 120000), // 会话租约：超过该时间未轮询即释放
    maxSessionMs: readInt(env, 'MP_GATEWAY_MAX_SESSION_MS', 2 * 60 * 60 * 1000, 60000, 24 * 60 * 60 * 1000), // 单会话最长存活时间
    maxSessions: readInt(env, 'MP_GATEWAY_MAX_SESSIONS', 4, 1, 100), // 全局最大小程序会话数（每个会话 1 路 Ingress 转码）
    maxRemotesPerSession: readInt(env, 'MP_GATEWAY_MAX_REMOTES_PER_SESSION', 4, 1, 16), // 单会话最多拉取的远端路数
    maxEgress: readInt(env, 'MP_GATEWAY_MAX_EGRESS', 8, 1, 200), // 全局最大 Egress 路数
    egressRetryMs: readInt(env, 'MP_GATEWAY_EGRESS_RETRY_MS', 10000, 1000, 120000), // 单路 Egress 失败后的冷却时间
    egressPublishTimeoutMs: readInt(env, 'MP_GATEWAY_EGRESS_PUBLISH_TIMEOUT_MS', 20000, 5000, 120000), // Egress 启动后等待 SRS on_publish 的最长时间
    cleanupRetryMs: readInt(env, 'MP_GATEWAY_CLEANUP_RETRY_MS', 5000, 1000, 60000), // 启动回收失败后的重试间隔
    releaseRetryMs: readInt(env, 'MP_GATEWAY_RELEASE_RETRY_MS', 5000, 1000, 60000), // Ingress / Egress 释放失败后的重试间隔；确认释放前继续占用配额
    requestTimeoutMs: readInt(env, 'MP_GATEWAY_REQUEST_TIMEOUT_MS', 10000, 1000, 60000), // 调用 LiveKit API 超时
    clockSkewSec: readInt(env, 'MP_GATEWAY_CLOCK_SKEW_SEC', 30, 0, 300), // 校验 nbf / exp 允许的时钟偏差
    egressEncoding: Object.freeze({ // live-player 兼容的 H264 Baseline + AAC 编码参数
      width: readInt(env, 'MP_GATEWAY_EGRESS_WIDTH', 640, 160, 1920),
      height: readInt(env, 'MP_GATEWAY_EGRESS_HEIGHT', 360, 120, 1080),
      framerate: readInt(env, 'MP_GATEWAY_EGRESS_FRAMERATE', 15, 5, 30),
      videoBitrate: readInt(env, 'MP_GATEWAY_EGRESS_VIDEO_KBPS', 600, 100, 4000),
      audioBitrate: readInt(env, 'MP_GATEWAY_EGRESS_AUDIO_KBPS', 64, 16, 256),
      keyFrameInterval: readInt(env, 'MP_GATEWAY_EGRESS_KEYFRAME_SEC', 1, 1, 10),
    }),
  })
}
