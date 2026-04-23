/**
 * shop.js — 店铺 API（模板开店 / 获取店铺）
 *
 * MVP策略：
 *   - createShop 优先存 localStorage（无后端时离线可用）
 *   - 同时尝试 POST /api/shop/create（有后端时同步到服务端）
 *   - getShop   先读 localStorage，无则请求后端
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

function defaultTheme() {
  return {
    bg:      '#0B0B0B',
    card:    '#111111',
    border:  '#1A1A1A',
    text:    '#FFFFFF',
    subText: '#9CA3AF',
    primary: '#00FF88',
  }
}
