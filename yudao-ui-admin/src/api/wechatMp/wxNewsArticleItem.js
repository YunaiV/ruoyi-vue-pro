import request from '@/utils/request'

// 创建图文消息文章列表表 
export function createWxNewsArticleItem(data) {
  return request({
    url: '/wechatMp/wx-news-article-item/create',
    method: 'post',
    data: data
  })
}

// 更新图文消息文章列表表 
export function updateWxNewsArticleItem(data) {
  return request({
    url: '/wechatMp/wx-news-article-item/update',
    method: 'put',
    data: data
  })
}

// 删除图文消息文章列表表 
export function deleteWxNewsArticleItem(id) {
  return request({
    url: '/wechatMp/wx-news-article-item/delete?id=' + id,
    method: 'delete'
  })
}

// 获得图文消息文章列表表 
export function getWxNewsArticleItem(id) {
  return request({
    url: '/wechatMp/wx-news-article-item/get?id=' + id,
    method: 'get'
  })
}

// 获得图文消息文章列表表 分页
export function getWxNewsArticleItemPage(query) {
  return request({
    url: '/wechatMp/wx-news-article-item/page',
    method: 'get',
    params: query
  })
}

// 导出图文消息文章列表表  Excel
export function exportWxNewsArticleItemExcel(query) {
  return request({
    url: '/wechatMp/wx-news-article-item/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
