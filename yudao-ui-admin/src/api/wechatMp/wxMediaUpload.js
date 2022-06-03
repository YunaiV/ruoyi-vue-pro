import request from '@/utils/request'

// 创建微信素材上传表 
export function createWxMediaUpload(data) {
  return request({
    url: '/wechatMp/wx-media-upload/create',
    method: 'post',
    data: data
  })
}

// 更新微信素材上传表 
export function updateWxMediaUpload(data) {
  return request({
    url: '/wechatMp/wx-media-upload/update',
    method: 'put',
    data: data
  })
}

// 删除微信素材上传表 
export function deleteWxMediaUpload(id) {
  return request({
    url: '/wechatMp/wx-media-upload/delete?id=' + id,
    method: 'delete'
  })
}

// 获得微信素材上传表 
export function getWxMediaUpload(id) {
  return request({
    url: '/wechatMp/wx-media-upload/get?id=' + id,
    method: 'get'
  })
}

// 获得微信素材上传表 分页
export function getWxMediaUploadPage(query) {
  return request({
    url: '/wechatMp/wx-media-upload/page',
    method: 'get',
    params: query
  })
}

// 导出微信素材上传表  Excel
export function exportWxMediaUploadExcel(query) {
  return request({
    url: '/wechatMp/wx-media-upload/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
