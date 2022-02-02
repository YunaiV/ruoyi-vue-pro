import request from '@/utils/request'

// 查询字典类型列表
export function listType(query) {
  return request({
    url: '/system/dict-type/page',
    method: 'get',
    params: query
  })
}

// 查询字典类型详细
export function getType(dictId) {
  return request({
    url: '/system/dict-type/get?id=' + dictId,
    method: 'get'
  })
}

// 新增字典类型
export function addType(data) {
  return request({
    url: '/system/dict-type/create',
    method: 'post',
    data: data
  })
}

// 修改字典类型
export function updateType(data) {
  return request({
    url: '/system/dict-type/update',
    method: 'put',
    data: data
  })
}

// 删除字典类型
export function delType(dictId) {
  return request({
    url: '/system/dict-type/delete?id=' + dictId,
    method: 'delete'
  })
}

// 导出字典类型
export function exportType(query) {
  return request({
    url: '/system/dict-type/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 获取字典选择框列表
export function listAllSimple() {
  return request({
    url: '/system/dict-type/list-all-simple',
    method: 'get'
  })
}
