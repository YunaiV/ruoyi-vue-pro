import { defHttp } from '@/config/axios'
import type { DeptVO } from './types'

// 查询部门（精简)列表
export const listSimpleDeptApi = () => {
  return defHttp.get({ url: '/system/dept/list-all-simple' })
}

// 查询部门列表
export const getDeptPageApi = ({ params }) => {
  return defHttp.get<PageResult<DeptVO>>({ url: '/system/dept/list', params })
}

// 查询部门详情
export const getDeptApi = (id: number) => {
  return defHttp.get<DeptVO>({ url: '/system/dept/get?id=' + id })
}

// 新增部门
export const createDeptApi = (params: DeptVO) => {
  return defHttp.post({ url: '/system/dept/create', data: params })
}

// 修改部门
export const updateDeptApi = (params: DeptVO) => {
  return defHttp.put({ url: '/system/dept/update', data: params })
}

// 删除部门
export const deleteDeptApi = (id: number) => {
  return defHttp.delete({ url: '/system/dept/delete?id=' + id })
}
