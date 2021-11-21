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
// export function getChannel(id) {
//   return request({
//     url: '/pay/channel/get?id=' + id,
//     method: 'get'
//   })
// }



// 获得支付渠道分页
export function getChannelPage(query) {
  return request({
    url: '/pay/channel/page',
    method: 'get',
    params: query
  })
}

// 导出支付渠道Excel
export function exportChannelExcel(query) {
  return request({
    url: '/pay/channel/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 获得支付渠道
export function getChannel(merchantId,appId,code) {
  return request({
    url: '/pay/channel/get-channel',
    params:{
      merchantId:merchantId,
      appId:appId,
      code:code
    },
    method: 'get'
  })
}

