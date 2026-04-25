export function initUserId() {
  let uid = uni.getStorageSync('deepay_uid')
  if (!uid) {
    uid = 'u_' + Math.random().toString(36).slice(2, 10) + '_' + Date.now().toString(36)
    uni.setStorageSync('deepay_uid', uid)
  }
  return uid
}

export function isLoggedIn() {
  return !!uni.getStorageSync('deepay_token')
}

export function formatDate(ts) {
  const d = new Date(ts)
  const now = new Date()
  const diff = now - d
  if (diff < 60000)  return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + ' 天前'
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

export function navigateTo(path) {
  uni.navigateTo({ url: '/pages/' + path })
}

export function switchTab(path) {
  uni.switchTab({ url: '/pages/' + path })
}
