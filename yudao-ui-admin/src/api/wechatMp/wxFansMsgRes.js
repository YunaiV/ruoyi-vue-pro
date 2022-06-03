import request from '@/utils/request'

// 创建回复粉丝消息历史表 
export function createWxFansMsgRes(data) {
  return request({
    url: '/wechatMp/wx-fans-msg-res/create',
    method: 'post',
    data: data
  })
}

// 更新回复粉丝消息历史表 
export function updateWxFansMsgRes(data) {
  return request({
    url: '/wechatMp/wx-fans-msg-res/update',
    method: 'put',
    data: data
  })
}

// 删除回复粉丝消息历史表 
export function deleteWxFansMsgRes(id) {
  return request({
    url: '/wechatMp/wx-fans-msg-res/delete?id=' + id,
    method: 'delete'
  })
}

// 获得回复粉丝消息历史表 
export function getWxFansMsgRes(id) {
  return request({
    url: '/wechatMp/wx-fans-msg-res/get?id=' + id,
    method: 'get'
  })
}

// 获得回复粉丝消息历史表 分页
export function getWxFansMsgResPage(query) {
  return request({
    url: '/wechatMp/wx-fans-msg-res/page',
    method: 'get',
    params: query
  })
}

// 导出回复粉丝消息历史表  Excel
export function exportWxFansMsgResExcel(query) {
  return request({
    url: '/wechatMp/wx-fans-msg-res/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
