// LiveKit 房间 Token 校验与网关管理 Token 签发：仅支持 HS256，与后端 LiveKitClient.signJoinToken 保持一致
import { createHmac, timingSafeEqual } from 'node:crypto'

const MAX_TOKEN_LENGTH = 4096 // 拒绝超长 Token，避免无意义的解析开销
const USER_IDENTITY_PATTERN = /^[1-9]\d{0,18}$/ // 业务 identity：数字 userId，与后端 buildIdentity 一致
const MAX_ROOM_LENGTH = 128 // 房间名长度上限

/** 房间 Token 校验失败；status 为返回给客户端的 HTTP 状态码 */
export class TokenError extends Error {
  /**
   * @param {string} code 错误码
   * @param {string} message 错误说明（不包含 Token 内容）
   * @param {number} status HTTP 状态码
   */
  constructor(code, message, status = 401) {
    super(message)
    this.code = code
    this.status = status
  }
}

/**
 * 解码 JWT 的 JSON 段
 *
 * @param {string} segment base64url 段
 * @returns {Record<string, any>} JSON 对象
 */
function decodeSegment(segment) {
  try {
    const value = JSON.parse(Buffer.from(segment, 'base64url').toString('utf8'))
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      return value
    }
  } catch {
    // 统一按格式错误处理
  }
  throw new TokenError('invalid_token', 'Token 格式错误')
}

/**
 * 计算 HS256 签名
 *
 * @param {string} signingInput header.payload
 * @param {string} secret 签名密钥
 * @returns {Buffer} 签名字节
 */
function sign(signingInput, secret) {
  return createHmac('sha256', secret).update(signingInput).digest()
}

/**
 * 校验后端签发的 LiveKit 房间 Token，并以 Token 内的房间与身份为准
 *
 * @param {string | undefined} token 房间 Token
 * @param {{ apiKey: string, apiSecret: string, clockSkewSec: number }} config 网关配置
 * @param {number} nowSec 当前时间（秒），便于测试
 * @returns {{ room: string, identity: string, name: string, exp: number }} 授权信息
 */
export function verifyRoomToken(token, config, nowSec = Math.floor(Date.now() / 1000)) {
  if (typeof token !== 'string' || !token || token.length > MAX_TOKEN_LENGTH) {
    throw new TokenError('missing_token', '缺少房间 Token')
  }
  const parts = token.split('.')
  if (parts.length !== 3 || parts.some(part => !/^[\w-]+$/.test(part))) {
    throw new TokenError('invalid_token', 'Token 格式错误')
  }
  const header = decodeSegment(parts[0])
  if (header.alg !== 'HS256' || (header.typ !== undefined && header.typ !== 'JWT')) {
    throw new TokenError('invalid_token', '仅支持 HS256 Token')
  }
  const expected = sign(`${parts[0]}.${parts[1]}`, config.apiSecret)
  const actual = Buffer.from(parts[2], 'base64url')
  if (actual.length !== expected.length || !timingSafeEqual(actual, expected)) {
    throw new TokenError('invalid_signature', 'Token 签名无效')
  }
  const payload = decodeSegment(parts[1])
  if (payload.iss !== config.apiKey) {
    throw new TokenError('invalid_issuer', 'Token 签发方不匹配')
  }
  if (typeof payload.exp !== 'number' || payload.exp + config.clockSkewSec <= nowSec) {
    throw new TokenError('token_expired', 'Token 已过期')
  }
  if (typeof payload.nbf !== 'number' || payload.nbf - config.clockSkewSec > nowSec) {
    throw new TokenError('token_not_active', 'Token 尚未生效')
  }
  if (typeof payload.sub !== 'string' || !USER_IDENTITY_PATTERN.test(payload.sub)) {
    throw new TokenError('invalid_identity', 'Token 身份不是业务用户', 403)
  }
  const video = payload.video
  if (!video || typeof video !== 'object'
    || video.roomJoin !== true
    || typeof video.room !== 'string' || !video.room || video.room.length > MAX_ROOM_LENGTH
    || video.canPublish !== true || video.canSubscribe !== true
    || video.hidden === true || video.recorder === true) {
    throw new TokenError('insufficient_grant', 'Token 缺少进房、发布或订阅授权', 403)
  }
  if (Array.isArray(video.canPublishSources)
    && !(video.canPublishSources.includes('camera') && video.canPublishSources.includes('microphone'))) {
    throw new TokenError('insufficient_grant', 'Token 不允许发布摄像头和麦克风', 403)
  }
  return {
    room: video.room,
    identity: payload.sub,
    name: typeof payload.name === 'string' && payload.name ? payload.name.slice(0, 128) : payload.sub,
    exp: payload.exp,
  }
}

/**
 * 签发调用 LiveKit Server API 的短期管理 Token
 *
 * @param {{ apiKey: string, apiSecret: string }} config 网关配置
 * @param {Record<string, any>} video 授权，如 { roomAdmin: true, room }
 * @param {number} ttlSec 有效期（秒）
 * @returns {string} JWT
 */
export function signAdminToken(config, video, ttlSec = 300) {
  const nowSec = Math.floor(Date.now() / 1000)
  const header = Buffer.from(JSON.stringify({ alg: 'HS256', typ: 'JWT' })).toString('base64url')
  const payload = Buffer.from(JSON.stringify({
    iss: config.apiKey,
    sub: 'yudao-mp-gateway',
    nbf: nowSec - 5,
    exp: nowSec + ttlSec,
    video,
  })).toString('base64url')
  return `${header}.${payload}.${sign(`${header}.${payload}`, config.apiSecret).toString('base64url')}`
}
