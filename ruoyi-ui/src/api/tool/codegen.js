import request from '@/utils/request'

// 获得表定义分页
export function getCodegenTablePage(query) {
  return request({
    url: '/tool/codegen/table/page',
    method: 'get',
    params: query
  })
}

// 获得表和字段的明细
export function getCodegenDetail(tableId) {
  return request({
    url: '/tool/codegen/detail?tableId=' + tableId,
    method: 'get',
  })
}

// 修改代码生成信息
export function updateCodegen(data) {
  return request({
    url: '/tool/codegen/update',
    method: 'put',
    data: data
  })
}

// 基于数据库的表结构，同步数据库的表和字段定义
export function syncCodegen(tableId) {
  return request({
    url: '/tool/codegen/sync?tableId=' + tableId,
    method: 'put'
  })
}

// 预览生成代码
export function previewCodegen(tableId) {
  return request({
    url: '/tool/codegen/preview?tableId=' + tableId,
    method: 'get',
  })
}

// 下载生成代码
export function downloadCodegen(tableId) {
  return request({
    url: '/tool/codegen/download?tableId=' + tableId,
    method: 'get',
    responseType: 'blob'
  })
}

// 获得表定义分页
export function getSchemaTableList(query) {
  return request({
    url: '/tool/codegen/db/table/list',
    method: 'get',
    params: query
  })
}

// 基于数据库的表结构，创建代码生成器的表定义
export function createCodegenList(tableNames) {
  return request({
    url: '/tool/codegen/create-list',
    method: 'post',
    headers:{
      'Content-type': 'application/x-www-form-urlencoded'
    },
    data: 'tableNames=' + tableNames
  })
}

// 删除数据库的表和字段定义
export function deleteCodegen(tableId) {
  return request({
    url: '/tool/codegen/delete?tableId=' + tableId,
    method: 'delete'
  })
}
