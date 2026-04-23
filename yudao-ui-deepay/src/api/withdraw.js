/**
 * withdraw.js — 提现 API
 * X-User-Id header injected automatically by request.js interceptor.
 *
 * 后端接口规范：
 *
 * POST /api/withdraw/apply
 *   body: { amount: 50, account: "paypal:xxx@gmail.com" }
 *   → { withdrawId, status: "pending" }
 *
 * GET  /api/withdraw/list
 *   → [{ id, amount, status, account, createdAt, processedAt }]
 *
 * GET  /api/user/balance
 *   → { balance, frozen, totalEarn }
 */
import http from '@/utils/request'

/**
 * 发起提现申请
 * @param {number} amount   提现金额（≥ 10）
 * @param {string} account  收款账户，e.g. "paypal:xxx@gmail.com"
 * @returns {Promise<{ withdrawId, status }>}
 */
export function applyWithdraw(amount, account) {
  return http.post('/api/withdraw/apply', { amount, account })
}

/**
 * 获取提现记录列表（最新在前）
 * @returns {Promise<Array<{ id, amount, status, account, createdAt, processedAt }>>}
 */
export function getWithdrawList() {
  return http.get('/api/withdraw/list')
}

/**
 * 获取用户余额详情
 * @returns {Promise<{ balance, frozen, totalEarn }>}
 */
export function getUserBalance() {
  return http.get('/api/user/balance')
}
