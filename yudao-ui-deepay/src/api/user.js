/**
 * user.js — 用户 / 佣金 API
 * X-User-Id header injected automatically by request.js interceptor.
 */
import http from '@/utils/request'

/**
 * 获取用户资料
 * @param {string} userId
 * @returns {Promise<{ userId, nickname, totalEarn, totalOrders }>}
 */
export function getProfile(userId) {
  return http.get('/api/user/profile', { params: { userId } })
}

/**
 * 获取佣金收益列表
 * @param {string} userId
 * @returns {Promise<{ total: number, list: Array }>}
 */
export function getEarnings(userId) {
  return http.get('/api/user/earnings', { params: { userId } })
}

/**
 * 获取收益排行榜（前50名）
 * @returns {Promise<Array<{ rank, userId, nickname, totalEarn }>>}
 */
export function getLeaderboard() {
  return http.get('/api/user/leaderboard')
}

/**
 * 获取当前用户的排名
 * @returns {Promise<{ rank: number, totalEarn: number }>}
 */
export function getMyRank() {
  return http.get('/api/user/myRank')
}

/**
 * 获取用户总资产面板（单接口，秒开）
 * @returns {Promise<{
 *   totalEarn: number, balance: number, frozen: number,
 *   inviteCount: number, inviteEarn: number,
 *   shareAmount: number, dividendEarn: number
 * }>}
 */
export function getDashboard() {
  return http.get('/api/user/dashboard')
}

/**
 * 获取平台累计数据（信任背书）
 * @returns {Promise<{ totalDividend: number, totalUsers: number, totalOrders: number }>}
 */
export function getPlatformStats() {
  return http.get('/api/platform/stats')
}
