import request from '@/utils/request'


// 获得我的站内信分页
export function getNotifyLogPage(query) {
  return request({
    url: '/system/notify-log/page',
    method: 'get',
    params: query
  })
}
