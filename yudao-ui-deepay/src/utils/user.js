/**
 * user.js — 轻量用户身份工具
 *
 * MVP阶段不需要登录——用 localStorage 维持一个稳定 userId。
 * 所有页面统一调用 initUserId()，保证同一浏览器永远拿到同一个 ID。
 *
 * 后接登录系统时，只需把 localStorage.setItem('deepay_uid', serverUserId)
 * 覆盖一次，整个 app 立刻切换成登录态。
 */

const KEY_UID = 'deepay_uid'
const KEY_REF = 'deepay_ref'

/**
 * 返回当前用户 ID；首次调用时自动生成并持久化。
 * @returns {string}  e.g. "u_1714000000000_x4f2"
 */
export function initUserId() {
  let uid = localStorage.getItem(KEY_UID)
  if (!uid) {
    uid = `u_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`
    localStorage.setItem(KEY_UID, uid)
  }
  return uid
}

/**
 * 记录推荐人（首次写入，不覆盖）。
 * 在 Shop.vue / 任何带 ?ref= 的落地页调用。
 * @param {string|null} refUserId
 * @param {string}      myUserId   当前用户自己的 ID（防止自刷）
 */
export function captureRef(refUserId, myUserId) {
  if (!refUserId) return
  if (refUserId === myUserId) return          // 不能给自己刷
  if (localStorage.getItem(KEY_REF)) return   // 已记录，不覆盖（防重置）
  localStorage.setItem(KEY_REF, refUserId)
}

/**
 * 返回已记录的推荐人 ID，没有则 null。
 * @returns {string|null}
 */
export function getRefUser() {
  return localStorage.getItem(KEY_REF) || null
}

/**
 * 生成带 ?ref=myUserId 的店铺分享链接。
 * @param {string} shopId
 * @param {string} myUserId
 * @returns {string}
 */
export function buildShareLink(shopId, myUserId) {
  return `${window.location.origin}/shop/${shopId}?ref=${myUserId}`
}

/**
 * 调用系统分享 / 降级到剪贴板。
 * @param {string} url
 * @param {string} title
 */
export function shareOrCopy(url, title = 'Deepay') {
  if (navigator.share) {
    navigator.share({ title, url }).catch(() => {})
  } else {
    navigator.clipboard?.writeText(url)
      .then(() => alert(`分享链接已复制 🎉\n${url}`))
      .catch(() => alert(`分享链接：\n${url}`))
  }
}
