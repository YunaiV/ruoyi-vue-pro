import request from '@/utils/request'


// 获得我的站内信分页
export function getNotifyMessagePage(query) {
  return request({
    url: '/system/notify-message/page',
    method: 'get',
    params: query
  })
}

// 获得单条我的站内信
export function getNotifyMessage(query) {
  return request({
    url: '/system/notify-message/get',
    method: 'get',
    params: query
  })
}

// 批量标记已读
export function updateNotifyMessageListRead(data) {
  return request({
    url: '/system/notify-message/update-list-read',
    method: 'put',
    data: data
  })
}

// 所有未读消息标记已读
export function updateNotifyMessageAllRead(data) {
  return request({
    url: '/system/notify-message/update-all-read',
    method: 'put',
    data: data
  })
}
