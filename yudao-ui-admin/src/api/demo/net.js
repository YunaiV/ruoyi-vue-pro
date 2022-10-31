import request from '@/utils/request'

// 创建区块链网络
export function createNet(data) {
  return request({
    url: '/demo/net/create',
    method: 'post',
    data: data
  })
}

// 更新区块链网络
export function updateNet(data) {
  return request({
    url: '/demo/net/update',
    method: 'put',
    data: data
  })
}

// 删除区块链网络
export function deleteNet(id) {
  return request({
    url: '/demo/net/delete?id=' + id,
    method: 'delete'
  })
}

// 获得区块链网络
export function getNet(id) {
  return request({
    url: '/demo/net/get?id=' + id,
    method: 'get'
  })
}

// 获得区块链网络分页
export function getNetPage(query) {
  return request({
    url: '/demo/net/page',
    method: 'get',
    params: query
  })
}

// 导出区块链网络 Excel
export function exportNetExcel(query) {
  return request({
    url: '/demo/net/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
