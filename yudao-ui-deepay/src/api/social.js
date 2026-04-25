/**
 * social.js — 社交电商 API
 *
 * 提供：
 *   createShareLink  — 生成带 ref 的短链接（本地生成，可选同步后端）
 *   getShareStats    — 获取分享链接点击/购买统计
 *   quickOrder       — 快速下单（透传到 /api/orders/quick）
 *   recordClick      — 记录分享链接点击（离线降级友好）
 *
 * 离线策略与 shop.js 一致：先本地，再尝试后端，失败不阻断 UI。
 */
import http from '@/utils/request'
import { initUserId, captureRef } from '@/utils/user'

const LS_LINKS_KEY = 'deepay_share_links'

/* ─── 工具 ──────────────────────────────────────────────────── */

/** 读取本地所有分享链接记录 */
function _readLinks() {
  try { return JSON.parse(localStorage.getItem(LS_LINKS_KEY) || '{}') } catch { return {} }
}

/** 写入本地分享链接记录 */
function _writeLinks(map) {
  localStorage.setItem(LS_LINKS_KEY, JSON.stringify(map))
}

/** 生成简短随机 id */
function _shortId() {
  return Math.random().toString(36).slice(2, 8)
}

/* ─── 公开 API ───────────────────────────────────────────────── */

/**
 * 生成专属分享链接
 * @param {string} shopId
 * @param {string} [productId]  — 指定商品（不传 = 全店链接）
 * @param {object} [options]    — { customAlias, expiresInDays }
 * @returns {{ linkId, fullUrl, shortCode, commissionRate, expiresAt }}
 */
export async function createShareLink(shopId, productId = null, options = {}) {
  const userId    = initUserId()
  const shortCode = options.customAlias || _shortId()
  const expiresAt = options.expiresInDays
    ? Date.now() + options.expiresInDays * 86_400_000
    : null

  // URL：/shop/<shopId>?ref=<userId>[&pid=<productId>]
  const origin = window.location.origin
  let fullUrl  = `${origin}/shop/${shopId}?ref=${userId}`
  if (productId) fullUrl += `&pid=${encodeURIComponent(productId)}`

  const record = {
    linkId:     `lnk_${shortCode}`,
    shortCode,
    shopId,
    productId,
    userId,
    fullUrl,
    expiresAt,
    clicks:     0,
    orders:     0,
    commission: 0,
    createdAt:  Date.now(),
  }

  // 本地持久化
  const map = _readLinks()
  map[record.linkId] = record
  _writeLinks(map)

  // 尝试同步后端（非阻断）
  try {
    const res = await http.post('/api/share/links/create', {
      shopId, productId, userId, shortCode, expiresAt,
    })
    if (res?.data?.linkId) record.linkId = res.data.linkId
  } catch (_) { /* 离线时静默 */ }

  return record
}

/**
 * 获取当前用户所有分享链接
 * @returns {Array}
 */
export function getMyShareLinks() {
  const userId = initUserId()
  const map    = _readLinks()
  return Object.values(map)
    .filter(l => l.userId === userId)
    .sort((a, b) => b.createdAt - a.createdAt)
}

/**
 * 获取单条链接统计（本地 + 后端合并）
 * @param {string} linkId
 * @returns {Promise<object>}
 */
/**
 * Compute derived metrics from raw stats.
 * @param {object} data  — must contain { clicks, orders, revenue }
 * @returns {object} data with conversionRate (%) and avgOrderValue appended
 */
function _enrichStats(data) {
  if (!data) return data
  const clicks  = data.clicks  || 0
  const orders  = data.orders  || 0
  const revenue = data.revenue || 0
  return {
    ...data,
    conversionRate: clicks > 0 ? +((orders / clicks) * 100).toFixed(2) : 0,
    avgOrderValue:  orders > 0 ? +(revenue / orders).toFixed(2) : 0,
  }
}

export async function getShareStats(linkId) {
  const map  = _readLinks()
  const local = map[linkId] || null

  try {
    const res = await http.get(`/api/share/stats/${linkId}`)
    return _enrichStats(res?.data || local)
  } catch {
    return _enrichStats(local)
  }
}

/**
 * 记录分享链接点击（落地页调用）
 * @param {string} linkId
 */
export async function recordClick(linkId) {
  if (!linkId) return
  // 更新本地计数
  const map = _readLinks()
  if (map[linkId]) {
    map[linkId].clicks = (map[linkId].clicks || 0) + 1
    _writeLinks(map)
  }
  // 后端上报（静默）
  try {
    await http.post('/api/share/click', { linkId, timestamp: Date.now() })
  } catch (_) { /* 离线降级 */ }
}

/**
 * 快速下单（社交电商链路）
 * @param {object} payload  { productId, shopId, quantity, size, buyerInfo, shareLinkId, paymentMethod }
 * @returns {Promise<{ orderId, payUrl, amount }>}
 */
export async function quickOrder(payload) {
  const refUser = payload.shareLinkId
    ? (_readLinks()[payload.shareLinkId]?.userId || null)
    : null

  return http.post('/api/orders/quick', {
    ...payload,
    refUser,
    buyerId: initUserId(),
  })
}

/**
 * 获取热门分享商品
 * @param {number} limit
 * @returns {Promise<Array>}
 */
export async function getHotShareProducts(limit = 10) {
  try {
    const res = await http.get(`/api/products/hot-shares?limit=${limit}`)
    return res?.data || []
  } catch {
    return []
  }
}

/**
 * 获取当前用户佣金汇总
 * @returns {Promise<{ pending, available, total, records }>}
 */
export async function getMyCommission() {
  const userId = initUserId()
  try {
    const res = await http.get(`/api/user/commission/${userId}`)
    return res?.data || { pending: 0, available: 0, total: 0, records: [] }
  } catch {
    // 本地估算（仅用于 UI 展示）
    const links  = getMyShareLinks()
    const total  = links.reduce((s, l) => s + (l.commission || 0), 0)
    return { pending: total * 0.7, available: total * 0.3, total, records: [] }
  }
}
