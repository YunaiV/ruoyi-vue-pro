import request from '@/utils/request'

// 获得公众号草稿分页
export function getDraftPage(query) {
  return request({
    url: '/mp/draft/page',
    method: 'get',
    params: query
  })
}

// 创建公众号草稿
export function createDraft(accountId, articles) {
  return request({
    url: '/mp/draft/create?accountId=' + accountId,
    method: 'post',
    data: {
      articles
    }
  })
}

// 更新公众号草稿
export function updateDraft(accountId, mediaId, articles) {
  return request({
    url: '/mp/draft/update?accountId=' + accountId + '&mediaId=' + mediaId,
    method: 'put',
    data: articles
  })
}

// 删除公众号草稿
export function deleteDraft(accountId, mediaId) {
  return request({
    url: '/mp/draft/delete?accountId=' + accountId + '&mediaId=' + mediaId,
    method: 'delete',
  })
}
