/**
 * invite.js — 邀请奖励 API
 * X-User-Id header injected automatically by request.js interceptor.
 */
import http from '@/utils/request'

/**
 * 获取当前用户的邀请统计
 * @returns {Promise<{ count: number, bonusEarn: number, list: Array<{ userId, amount }> }>}
 */
export function getInvites() {
  return http.get('/api/user/invites')
}
