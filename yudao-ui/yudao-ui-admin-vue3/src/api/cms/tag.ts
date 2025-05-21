import request from '@/utils/request'

// Define the TagVO interface based on CmsTagRespVO and other VOs
export interface TagVO {
  id?: number // Optional for creation
  name: string
  slug: string
  createTime?: Date // Usually provided in responses
}

// API for creating a tag
export const createTag = (data: TagVO) => {
  return request({
    url: '/cms/admin/tags/create',
    method: 'post',
    data
  })
}

// API for updating a tag
export const updateTag = (data: TagVO) => {
  return request({
    url: '/cms/admin/tags/update',
    method: 'put',
    data
  })
}

// API for deleting a tag
export const deleteTag = (id: number) => {
  return request({
    url: '/cms/admin/tags/delete',
    method: 'delete',
    params: { id } // Pass id as a query parameter
  })
}

// API for getting a specific tag
export const getTag = (id: number) => {
  return request({
    url: '/cms/admin/tags/get',
    method: 'get',
    params: { id } // Pass id as a query parameter
  })
}

// API for getting a paginated list of tags
// params should match CmsTagPageReqVO: { pageNo, pageSize, name, slug }
export const getTagPage = (params: any) => {
  return request({
    url: '/cms/admin/tags/page',
    method: 'get',
    params
  })
}

// API for getting a simple list of tags (for selections, etc.)
// The backend controller for CmsTag uses CmsTagPageReqVO for its list-simple endpoint too.
export const getSimpleTagList = (params?: any) => { // params can be { name, slug }
  return request({
    url: '/cms/admin/tags/list-simple',
    method: 'get',
    params
  })
}
