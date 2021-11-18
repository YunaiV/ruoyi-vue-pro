import request from '@/utils/request'


export function page(query) {
  return request({
    url: '/workflow/models/page',
    method: 'get',
    params: query
  })
}
