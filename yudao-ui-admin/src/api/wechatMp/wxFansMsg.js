import request from '@/utils/request'

// 创建粉丝消息表 
export function createWxFansMsg(data) {
  return request({
    url: '/wechatMp/wx-fans-msg/create',
    method: 'post',
    data: data
  })
}

// 更新粉丝消息表 
export function updateWxFansMsg(data) {
  return request({
    url: '/wechatMp/wx-fans-msg/update',
    method: 'put',
    data: data
  })
}

// 删除粉丝消息表 
export function deleteWxFansMsg(id) {
  return request({
    url: '/wechatMp/wx-fans-msg/delete?id=' + id,
    method: 'delete'
  })
}

// 获得粉丝消息表 
export function getWxFansMsg(id) {
  return request({
    url: '/wechatMp/wx-fans-msg/get?id=' + id,
    method: 'get'
  })
}

// 获得粉丝消息表 分页
export function getWxFansMsgPage(query) {
  return request({
    url: '/wechatMp/wx-fans-msg/page',
    method: 'get',
    params: query
  })
}

// 导出粉丝消息表  Excel
export function exportWxFansMsgExcel(query) {
  return request({
    url: '/wechatMp/wx-fans-msg/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
