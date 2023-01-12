import request from '@/utils/request'

// 获得公众号素材分页
export function getMaterialPage(query) {
  return request({
    url: '/mp/material/page',
    method: 'get',
    params: query
  })
}
