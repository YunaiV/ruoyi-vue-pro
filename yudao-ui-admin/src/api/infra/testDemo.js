import request from '@/utils/request'

// 创建字典类型
export function createTestDemo(data) {
  return request({
    url: '/infra/test-demo/create',
    method: 'post',
    data: data
  })
}

// 更新字典类型
export function updateTestDemo(data) {
  return request({
    url: '/infra/test-demo/update',
    method: 'put',
    data: data
  })
}

// 删除字典类型
export function deleteTestDemo(id) {
  return request({
    url: '/infra/test-demo/delete?id=' + id,
    method: 'delete'
  })
}

// 获得字典类型
export function getTestDemo(id) {
  return request({
    url: '/infra/test-demo/get?id=' + id,
    method: 'get'
  })
}

// 获得字典类型分页
export function getTestDemoPage(query) {
  return request({
    url: '/infra/test-demo/page',
    method: 'get',
    params: query
  })
}

// 导出字典类型 Excel
export function exportTestDemoExcel(query) {
  return request({
    url: '/infra/test-demo/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
