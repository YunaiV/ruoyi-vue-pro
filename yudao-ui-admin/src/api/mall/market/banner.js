import request from '@/utils/request'

// 创建Banner
export function createBanner(data) {
  return request({
    url: '/market/banner/create',
    method: 'post',
    data: data
  })
}

// 更新Banner
export function updateBanner(data) {
  return request({
    url: '/market/banner/update',
    method: 'put',
    data: data
  })
}

// 删除Banner
export function deleteBanner(id) {
  return request({
    url: '/market/banner/delete?id=' + id,
    method: 'delete'
  })
}

// 获得Banner
export function getBanner(id) {
  return request({
    url: '/market/banner/get?id=' + id,
    method: 'get'
  })
}

// 获得Banner分页
export function getBannerPage(query) {
  return request({
    url: '/market/banner/page',
    method: 'get',
    params: query
  })
}

// 导出Banner Excel
export function exportBannerExcel(query) {
  return request({
    url: '/market/banner/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
