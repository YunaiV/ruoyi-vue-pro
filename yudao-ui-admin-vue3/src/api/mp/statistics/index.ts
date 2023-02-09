import request from '@/config/axios'

// 获取消息发送概况数据
export function getUpstreamMessage(query) {
  return request.get({
    url: '/mp/statistics/upstream-message',
    params: query
  })
}

// 用户增减数据
export function getUserSummary(query) {
  return request.get({
    url: '/mp/statistics/user-summary',
    params: query
  })
}

// 获得用户累计数据
export function getUserCumulate(query) {
  return request.get({
    url: '/mp/statistics/user-cumulate',
    params: query
  })
}

// 获得接口分析数据
export function getInterfaceSummary(query) {
  return request.get({
    url: '/mp/statistics/interface-summary',
    params: query
  })
}
