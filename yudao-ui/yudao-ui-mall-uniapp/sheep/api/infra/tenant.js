import request from '@/sheep/request';

/**
 * 通过网站域名获取租户信息
 * @param {string} website - 网站域名
 * @returns {Promise<Object>} 租户信息
 */
export function getTenantByWebsite(website) {
  return request({
    url: '/system/tenant/get-by-website',
    method: 'GET',
    params: { website },
    custom: {
      isToken: false, // 避免登录情况下，跨租户访问被拦截
    },
  });
}
