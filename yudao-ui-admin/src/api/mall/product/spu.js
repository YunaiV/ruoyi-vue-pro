import request from '@/utils/request'

// 创建商品spu
export function createSpu(data) {
  return request({
    url: '/product/spu/create',
    method: 'post',
    data: data
  })
}

// 更新商品spu
export function updateSpu(data) {
  return request({
    url: '/product/spu/update',
    method: 'put',
    data: data
  })
}

// 删除商品spu
export function deleteSpu(id) {
  return request({
    url: '/product/spu/delete?id=' + id,
    method: 'delete'
  })
}

// 获得商品spu
export function getSpu(id) {
  return request({
    url: '/product/spu/get?id=' + id,
    method: 'get'
  })
}

// 获得商品spu分页
export function getSpuPage(query) {
  return request({
    url: '/product/spu/page',
    method: 'get',
    params: query
  })
}

// 导出商品spu Excel
export function exportSpuExcel(query) {
  return request({
    url: '/product/spu/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
