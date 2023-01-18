import request from '@/utils/request'

// 获得公众号素材分页
export function getMaterialPage(query) {
  return request({
    url: '/mp/material/page',
    method: 'get',
    params: query
  })
}

// 删除公众号永久素材
export function deletePermanentMaterial(id) {
  return request({
    url: '/mp/material/delete-permanent?id=' + id,
    method: 'delete'
  })
}
