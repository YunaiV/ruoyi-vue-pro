import request from '@/utils/request'

// 创建区块链节点
export function createEndpoint(data) {
  return request({
    url: '/demo/endpoint/create',
    method: 'post',
    data: data
  })
}

// 更新区块链节点
export function updateEndpoint(data) {
  return request({
    url: '/demo/endpoint/update',
    method: 'put',
    data: data
  })
}

// 删除区块链节点
export function deleteEndpoint(id) {
  return request({
    url: '/demo/endpoint/delete?id=' + id,
    method: 'delete'
  })
}

// 获得区块链节点
export function getEndpoint(id) {
  return request({
    url: '/demo/endpoint/get?id=' + id,
    method: 'get'
  })
}

// 获得区块链节点分页
export function getEndpointPage(query) {
  return request({
    url: '/demo/endpoint/page',
    method: 'get',
    params: query
  })
}

// 导出区块链节点 Excel
export function exportEndpointExcel(query) {
  return request({
    url: '/demo/endpoint/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
