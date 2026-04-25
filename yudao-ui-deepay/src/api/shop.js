/**
 * shop.js — 店铺 API（模板开店 / 获取店铺 / 商品管理）
 *
 * MVP策略：
 *   - createShop  优先存 localStorage（无后端时离线可用），同时尝试同步后端
 *   - getShop     先读 localStorage，无则请求后端
 *   - updateShop  本地写 + 尝试后端 PUT（失败静默）
 *   - addProduct / removeProduct / updateProduct  纯本地 + 静默同步
 * X-User-Id header injected automatically by request.js interceptor.
 */
import http from '@/utils/request'

/**
 * 创建店铺
 * @param {object} payload  { templateId, products, theme, name, type, gradient, style }
 * @returns {Promise<{ shopId: string }>}
 */
export async function createShop(payload) {
  const localId = String(Date.now())

  // 统一 shop 结构存 localStorage
  const shopData = {
    id:       payload.templateId || payload.id || 'minimal',
    type:     payload.type       || payload.templateId || 'minimal',
    name:     payload.name       || 'My Shop',
    theme:    payload.theme      || defaultTheme(),
    gradient: payload.gradient   || '#111',
    products: payload.products   || [],
    style:    payload.style      || null,
  }
  localStorage.setItem(`shop_${localId}`, JSON.stringify(shopData))

  // 尝试同步到后端（失败不影响离线使用）
  try {
    const res = await http.post('/api/shop/create', { ...payload, localId })
    const serverId = res?.shopId || res?.id || localId
    // 如果服务端返回不同 id，把本地数据迁移过去
    if (serverId !== localId) {
      localStorage.setItem(`shop_${serverId}`, JSON.stringify(shopData))
      localStorage.removeItem(`shop_${localId}`)
    }
    return { shopId: serverId }
  } catch (_) {
    // 后端不可用 → 纯离线模式
    return { shopId: localId }
  }
}

/**
 * 获取店铺数据
 * @param {string} id
 * @returns {Promise<object>} shop
 */
export async function getShop(id) {
  // ① localStorage 优先
  const cached = localStorage.getItem(`shop_${id}`)
  if (cached) {
    try { return JSON.parse(cached) } catch (_) {}
  }

  // ② 后端
  return http.get(`/api/shop/${id}`)
}

/**
 * 列出本地所有店铺（按创建时间倒序）
 * @returns {Array<{ shopId: string, name: string, ... }>}
 */
export function listMyShops() {
  const result = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key?.startsWith('shop_')) {
      try {
        const data = JSON.parse(localStorage.getItem(key))
        result.push({ shopId: key.replace('shop_', ''), ...data })
      } catch (_) {}
    }
  }
  return result.sort((a, b) => Number(b.shopId) - Number(a.shopId))
}

/**
 * 更新店铺字段（本地写 + 尝试同步后端）
 * @param {string} shopId
 * @param {object} patch  { name?, products?, theme?, gradient?, ... }
 * @returns {object} updated shop
 */
export async function updateShop(shopId, patch) {
  const raw  = localStorage.getItem(`shop_${shopId}`)
  const shop = raw ? { ...JSON.parse(raw), ...patch } : { ...patch }
  localStorage.setItem(`shop_${shopId}`, JSON.stringify(shop))
  try { await http.put(`/api/shop/${shopId}`, patch) } catch (_) {}
  return shop
}

/**
 * 向店铺追加一个商品
 * @param {string} shopId
 * @param {{ name, price, desc?, badge?, img? }} product
 * @returns {object|null} updated shop
 */
export async function addProduct(shopId, product) {
  const raw = localStorage.getItem(`shop_${shopId}`)
  if (!raw) return null
  const shop = JSON.parse(raw)
  shop.products = [...(shop.products || []), product]
  localStorage.setItem(`shop_${shopId}`, JSON.stringify(shop))
  try { await http.post(`/api/shop/${shopId}/products`, product) } catch (_) {}
  return shop
}

/**
 * 删除指定索引的商品
 * @param {string} shopId
 * @param {number} index
 * @returns {object|null} updated shop
 */
export async function removeProduct(shopId, index) {
  const raw = localStorage.getItem(`shop_${shopId}`)
  if (!raw) return null
  const shop = JSON.parse(raw)
  shop.products = (shop.products || []).filter((_, i) => i !== index)
  localStorage.setItem(`shop_${shopId}`, JSON.stringify(shop))
  try { await http.delete(`/api/shop/${shopId}/products/${index}`) } catch (_) {}
  return shop
}

/**
 * 更新指定索引的商品字段
 * @param {string} shopId
 * @param {number} index
 * @param {object} patch
 * @returns {object|null} updated shop
 */
export async function updateProduct(shopId, index, patch) {
  const raw = localStorage.getItem(`shop_${shopId}`)
  if (!raw) return null
  const shop = JSON.parse(raw)
  if (!shop.products?.[index]) return null
  shop.products[index] = { ...shop.products[index], ...patch }
  localStorage.setItem(`shop_${shopId}`, JSON.stringify(shop))
  try { await http.put(`/api/shop/${shopId}/products/${index}`, patch) } catch (_) {}
  return shop
}

/**
 * 搜索商品
 * @param {string} query               — 关键词
 * @param {object} [options]           — { category, minPrice, maxPrice, page, limit }
 * @returns {Promise<Array>}           — 商品列表，后端不可用时返回空数组
 */
export async function searchProducts(query, options = {}) {
  try {
    const res = await http.get('/api/products/search', {
      params: { q: query, ...options },
    })
    return res?.data || []
  } catch (_) {
    return []
  }
}

function defaultTheme() {
  return {
    bg:      '#0B0B0B',
    card:    '#111111',
    border:  '#1A1A1A',
    text:    '#FFFFFF',
    subText: '#9CA3AF',
    primary: '#1abc9c',
  }
}
