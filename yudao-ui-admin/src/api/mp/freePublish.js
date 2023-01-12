import request from '@/utils/request'

// 获得公众号素材分页
export function getFreePublishPage(query) {
  return request({
    url: '/mp/free-publish/page',
    method: 'get',
    params: query
  })
}
