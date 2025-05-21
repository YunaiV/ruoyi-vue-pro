import request from '@/utils/request'

// Define the CategoryVO interface based on CmsCategoryRespVO and other VOs
// This helps with type safety. It should match the backend VO structure.
export interface CategoryVO {
  id?: number // Optional for creation
  name: string
  slug: string
  parentId?: number | null // Can be null for root categories
  description?: string
  createTime?: Date // Usually provided in responses
  // Add other fields from CmsCategoryRespVO if needed
}

// API for creating a category
export const createCategory = (data: CategoryVO) => {
  return request({
    url: '/cms/admin/categories/create',
    method: 'post',
    data
  })
}

// API for updating a category
export const updateCategory = (data: CategoryVO) => {
  return request({
    url: '/cms/admin/categories/update',
    method: 'put',
    data
  })
}

// API for deleting a category
export const deleteCategory = (id: number) => {
  return request({
    // In CmsCategoryController.java, delete uses @RequestParam, so it should be a query param
    url: '/cms/admin/categories/delete',
    method: 'delete',
    params: { id } // Pass id as a query parameter
  })
}

// API for getting a specific category
export const getCategory = (id: number) => {
  return request({
    // In CmsCategoryController.java, get uses @RequestParam
    url: '/cms/admin/categories/get',
    method: 'get',
    params: { id } // Pass id as a query parameter
  })
}

// API for getting a paginated list of categories
export const getCategoryPage = (params: any) => { // params should match CmsCategoryPageReqVO
  return request({
    url: '/cms/admin/categories/page',
    method: 'get',
    params
  })
}

// API for getting a simple list of categories (for parent selection, etc.)
// The backend controller uses CmsCategoryPageReqVO for this, so params can be passed if needed for filtering
export const getSimpleCategoryList = (params?: any) => {
  return request({
    url: '/cms/admin/categories/list-simple',
    method: 'get',
    params // Optional params for filtering, e.g., parentId
  })
}
