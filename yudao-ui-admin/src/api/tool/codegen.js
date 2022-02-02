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
export function syncCodegenFromDB(tableId) {
  return request({
    url: '/tool/codegen/sync-from-db?tableId=' + tableId,
    method: 'put'
  })
}

// 基于 SQL 建表语句，同步数据库的表和字段定义
export function syncCodegenFromSQL(tableId, sql) {
  return request({
    url: '/tool/codegen/sync-from-sql?tableId=' + tableId,
    method: 'put',
    headers:{
      'Content-type': 'application/x-www-form-urlencoded'
    },
    data: 'tableId=' + tableId + "&sql=" + sql,
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
export function createCodegenListFromDB(tableNames) {
  return request({
    url: '/tool/codegen/create-list-from-db',
    method: 'post',
    headers:{
      'Content-type': 'application/x-www-form-urlencoded'
    },
    data: 'tableNames=' + tableNames
  })
}

// 基于 SQL 建表语句，创建代码生成器的表定义
export function createCodegenListFromSQL(data) {
  return request({
    url: '/tool/codegen/create-list-from-sql',
    method: 'post',
    headers:{
      'Content-type': 'application/x-www-form-urlencoded'
    },
    data: 'sql=' + data.sql,
  })
}

// 删除数据库的表和字段定义
export function deleteCodegen(tableId) {
  return request({
    url: '/tool/codegen/delete?tableId=' + tableId,
    method: 'delete'
  })
}
