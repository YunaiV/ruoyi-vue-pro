import request from '@/config/axios'

// 获得公众号素材分页
export const getFreePublishPage = (query) => {
  return request.get({
    url: '/mp/free-publish/page',
    params: query
  })
}

// 删除公众号素材
export const deleteFreePublish = (accountId, articleId) => {
  return request.delete({
    url: '/mp/free-publish/delete?accountId=' + accountId + '&articleId=' + articleId
  })
}

// 发布公众号素材
export const submitFreePublish = (accountId, mediaId) => {
  return request.post({
    url: '/mp/free-publish/submit?accountId=' + accountId + '&mediaId=' + mediaId
  })
}
