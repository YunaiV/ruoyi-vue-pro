import request from '@/utils/request'

// 创建微信菜单
export function createWxMenu(data) {
  return request({
    url: '/wechatMp/wx-menu/create',
    method: 'post',
    data: data
  })
}

// 更新微信菜单
export function updateWxMenu(data) {
  return request({
    url: '/wechatMp/wx-menu/update',
    method: 'put',
    data: data
  })
}

// 删除微信菜单
export function deleteWxMenu(id) {
  return request({
    url: '/wechatMp/wx-menu/delete?id=' + id,
    method: 'delete'
  })
}

// 获得微信菜单
export function getWxMenu(id) {
  return request({
    url: '/wechatMp/wx-menu/get?id=' + id,
    method: 'get'
  })
}

// 获得微信菜单分页
export function getWxMenuPage(query) {
  return request({
    url: '/wechatMp/wx-menu/page',
    method: 'get',
    params: query
  })
}

// 导出微信菜单 Excel
export function exportWxMenuExcel(query) {
  return request({
    url: '/wechatMp/wx-menu/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
