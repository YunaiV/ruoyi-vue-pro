import request from '@/utils/request'
import type { LocalDateTime } from '@/types/java.time' // Assuming a type definition for LocalDateTime

// Define the ArticleVO interface based on CmsArticleRespVO and other VOs
export interface ArticleVO {
  id?: number
  title: string
  slug: string
  content: string
  authorId?: number // Or string, depending on backend user ID type
  categoryId: number
  status?: number // 0: draft, 1: published
  publishedAt?: LocalDateTime | string // Or Date string
  coverImageUrl?: string
  views?: number
  metaDescription?: string
  metaKeywords?: string
  tagIds?: number[] // List of Tag IDs
  createTime?: LocalDateTime | string // Usually provided in responses

  // Enriched fields (optional, mainly for responses)
  categoryName?: string
  // tags?: TagSimpleRespVO[] // If backend provides enriched tag objects
}

// API for creating an article
export const createArticle = (data: ArticleVO) => {
  return request({
    url: '/cms/admin/articles/create',
    method: 'post',
    data
  })
}

// API for updating an article
export const updateArticle = (data: ArticleVO) => {
  return request({
    url: '/cms/admin/articles/update',
    method: 'put',
    data
  })
}

// API for deleting an article
export const deleteArticle = (id: number) => {
  return request({
    url: '/cms/admin/articles/delete',
    method: 'delete',
    params: { id }
  })
}

// API for getting a specific article
export const getArticle = (id: number) => {
  return request({
    url: '/cms/admin/articles/get',
    method: 'get',
    params: { id }
  })
}

// API for getting a paginated list of articles
// params should match CmsArticlePageReqVO: { pageNo, pageSize, title, categoryId, status, tagId }
export const getArticlePage = (params: any) => {
  return request({
    url: '/cms/admin/articles/page',
    method: 'get',
    params
  })
}

// API for publishing an article
export const publishArticle = (id: number) => {
  return request({
    url: '/cms/admin/articles/publish', // Assuming POST for actions without request body
    method: 'post', // Or PUT, check backend controller mapping
    params: { id }   // Or data: { id } if backend expects it in body
  })
}

// API for unpublishing an article
export const unpublishArticle = (id: number) => {
  return request({
    url: '/cms/admin/articles/unpublish', // Assuming POST for actions
    method: 'post', // Or PUT
    params: { id }    // Or data: { id }
  })
}
