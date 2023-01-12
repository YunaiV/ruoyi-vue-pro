import request from '@/utils/request'

// 获得公众号草稿分页
export function getDraftPage(query) {
  return request({
    url: '/mp/draft/page',
    method: 'get',
    params: query
  })
}
