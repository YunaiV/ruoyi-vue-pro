import request from '@/utils/request'

// TODO 获得公众号账号分页
export function getInterfaceSummary(query) {
  return request({
    url: '/mp/account/page',
    method: 'get',
    params: query
  })
}

// TODO 获得公众号账号分页
export function getUserSummary(query) {
  return request({
    url: '/mp/statistics/user-summary',
    method: 'get',
    params: query
  })
}

// TODO 获得公众号账号分页
export function getUserCumulate(query) {
  return request({
    url: '/mp/account/page',
    method: 'get',
    params: query
  })
}
