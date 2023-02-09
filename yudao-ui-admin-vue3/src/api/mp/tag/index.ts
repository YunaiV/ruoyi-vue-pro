import request from '@/config/axios'

// 创建公众号标签
export const createTag = (data) => {
  return request.post({
    url: '/mp/tag/create',
    data: data
  })
}

// 更新公众号标签
export const updateTag = (data) => {
  return request.put({
    url: '/mp/tag/update',
    data: data
  })
}

// 删除公众号标签
export const deleteTag = (id) => {
  return request.delete({
    url: '/mp/tag/delete?id=' + id
  })
}

// 获得公众号标签
export const getTag = (id) => {
  return request.get({
    url: '/mp/tag/get?id=' + id
  })
}

// 获得公众号标签分页
export const getTagPage = (query) => {
  return request.get({
    url: '/mp/tag/page',
    params: query
  })
}

// 获取公众号标签精简信息列表
export const getSimpleTags = () => {
  return request.get({
    url: '/mp/tag/list-all-simple'
  })
}

// 同步公众号标签
export const syncTag = (accountId) => {
  return request.post({
    url: '/mp/tag/sync?accountId=' + accountId
  })
}
