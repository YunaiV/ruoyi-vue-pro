import request from '@/utils/request'

// 创建短信渠道
export function createSmsChannel(data) {
  return request({
    url: '/system/sms-channel/create',
    method: 'post',
    data: data
  })
}

// 更新短信渠道
export function updateSmsChannel(data) {
  return request({
    url: '/system/sms-channel/update',
    method: 'put',
    data: data
  })
}

// 删除短信渠道
export function deleteSmsChannel(id) {
  return request({
    url: '/system/sms-channel/delete?id=' + id,
    method: 'delete'
  })
}

// 获得短信渠道
export function getSmsChannel(id) {
  return request({
    url: '/system/sms-channel/get?id=' + id,
    method: 'get'
  })
}

// 获得短信渠道分页
export function getSmsChannelPage(query) {
  return request({
    url: '/system/sms-channel/page',
    method: 'get',
    params: query
  })
}

// 获得短信渠道精简列表
export function getSimpleSmsChannels() {
  return request({
    url: '/system/sms-channel/list-all-simple',
    method: 'get',
  })
}
