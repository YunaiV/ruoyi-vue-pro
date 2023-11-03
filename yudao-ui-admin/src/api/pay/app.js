import request from '@/utils/request'

// 创建支付应用
export function createApp(data) {
  return request({
    url: '/pay/app/create',
    method: 'post',
    data: data
  })
}

// 更新支付应用
export function updateApp(data) {
  return request({
    url: '/pay/app/update',
    method: 'put',
    data: data
  })
}

// 支付应用状态修改
export function changeAppStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
    url: '/pay/app/update-status',
    method: 'put',
    data: data
  })
}

// 删除支付应用
export function deleteApp(id) {
  return request({
    url: '/pay/app/delete?id=' + id,
    method: 'delete'
  })
}

// 获得支付应用
export function getApp(id) {
  return request({
    url: '/pay/app/get?id=' + id,
    method: 'get'
  })
}

// 获得支付应用分页
export function getAppPage(query) {
  return request({
    url: '/pay/app/page',
    method: 'get',
    params: query
  })
}

// 获得支付应用列表
export function getAppList() {
  return request({
    url: '/pay/app/list',
    method: 'get'
  })
}
