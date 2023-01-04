import request from '@/utils/request'

// 获得地区树
export function getAreaTree() {
  return request({
    url: '/system/area/tree',
    method: 'get'
  })
}

// 获得 IP 对应的地区名
export function getAreaByIp(ip) {
  return request({
    url: '/system/area/get-by-ip?ip=' + ip,
    method: 'get'
  })
}
