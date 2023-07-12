import request from '@/utils/request'

// 创建支付渠道
export function createChannel(data) {
  return request({
    url: '/pay/channel/create',
    method: 'post',
    data: data
  })
}


// 更新支付渠道
export function updateChannel(data) {
  return request({
    url: '/pay/channel/update',
    method: 'put',
    data: data
  })
}

// 删除支付渠道
export function deleteChannel(id) {
  return request({
    url: '/pay/channel/delete?id=' + id,
    method: 'delete'
  })
}

// 获得支付渠道
export function getChannel(appId,code) {
  return request({
    url: '/pay/channel/get-channel',
    params:{
      appId:appId,
      code:code
    },
    method: 'get'
  })
}

