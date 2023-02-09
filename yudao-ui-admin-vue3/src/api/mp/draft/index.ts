import request from '@/config/axios'

// 获得公众号草稿分页
export const getDraftPage = (query) => {
  return request.get({
    url: '/mp/draft/page',
    params: query
  })
}

// 创建公众号草稿
export const createDraft = (accountId, articles) => {
  return request.post({
    url: '/mp/draft/create?accountId=' + accountId,
    data: {
      articles
    }
  })
}

// 更新公众号草稿
export const updateDraft = (accountId, mediaId, articles) => {
  return request.put({
    url: '/mp/draft/update?accountId=' + accountId + '&mediaId=' + mediaId,
    method: 'put',
    data: articles
  })
}

// 删除公众号草稿
export const deleteDraft = (accountId, mediaId) => {
  return request.delete({
    url: '/mp/draft/delete?accountId=' + accountId + '&mediaId=' + mediaId
  })
}
