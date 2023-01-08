import request from '@/utils/request'

// 创建公众号标签
export function createTag(data) {
  return request({
    url: '/mp/tag/create',
    method: 'post',
    data: data
  })
}

// 更新公众号标签
export function updateTag(data) {
  return request({
    url: '/mp/tag/update',
    method: 'put',
    data: data
  })
}

// 删除公众号标签
export function deleteTag(id) {
  return request({
    url: '/mp/tag/delete?id=' + id,
    method: 'delete'
  })
}

// 获得公众号标签
export function getTag(id) {
  return request({
    url: '/mp/tag/get?id=' + id,
    method: 'get'
  })
}

// 获得公众号标签分页
export function getTagPage(query) {
  return request({
    url: '/mp/tag/page',
    method: 'get',
    params: query
  })
}

// 获取公众号标签精简信息列表
export function getSimpleTags() {
  return request({
    url: '/mp/tag/list-all-simple',
    method: 'get',
  })
}

// 同步公众号标签
export function syncTag(accountId) {
  return request({
    url: '/mp/tag/sync?accountId=' + accountId,
    method: 'post'
  })
}
