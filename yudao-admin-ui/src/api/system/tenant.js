import request from '@/utils/request'

// 使用租户名，获得租户编号
export function getTenantIdByName(name) {
  return request({
    url: '/system/tenant/get-id-by-name',
    method: 'get',
    params: {
      name
    }
  })
}
