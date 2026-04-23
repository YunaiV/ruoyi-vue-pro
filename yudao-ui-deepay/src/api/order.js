/**
 * order.js — order & payment API
 * X-User-Id header injected automatically by request.js interceptor.
 */
import http from '@/utils/request'

/**
 * Create a product purchase order.
 * Returns { orderId, payUrl }
 * @param {string} shopId
 * @param {number} amount    decimal e.g. 29.99
 * @param {string} currency  e.g. 'EUR'
 * @param {string|null} refUser  referral user id (first-touch attribution)
 */
export function createOrder(shopId, amount, currency = 'EUR', refUser = null) {
  return http.post('/api/order/create', { shopId, amount, currency, refUser })
}

/** Create a quota payment order → { payUrl, paymentId, priceEur } */
export function createPayment(userId, planId) {
  return http.post('/api/pay/create', { userId, plan: planId })
}

/** Get shop product page data */
export function getShopPage(id, currency = 'EUR') {
  return http.get(`/api/shop/${id}`, { params: { currency } })
}
