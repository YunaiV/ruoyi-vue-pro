import { defHttp } from '@/config/axios'
import type { PostVO } from './types'

// 查询岗位列表
export const getPostPageApi = ({ params }) => {
  return defHttp.get<PageResult<PostVO>>({ url: '/system/post/page', params })
}

// 获取岗位精简信息列表
export const listSimplePostsApi = () => {
  return defHttp.get<PostVO[]>({ url: '/system/post/list-all-simple' })
}
// 查询岗位详情
export const getPostApi = (id: number) => {
  return defHttp.get<PostVO>({ url: '/system/post/get?id=' + id })
}

// 新增岗位
export const createPostApi = (params: PostVO) => {
  return defHttp.post({ url: '/system/post/create', params })
}

// 修改岗位
export const updatePostApi = (params: PostVO) => {
  return defHttp.put({ url: '/system/post/update', params })
}

// 删除岗位
export const deletePostApi = (id: number) => {
  return defHttp.delete({ url: '/system/post/delete?id=' + id })
}

// 导出岗位
export const exportPostApi = (params) => {
  return defHttp.get({ url: '/system/post/export', params, responseType: 'blob' })
}
