import { useAxios } from '@/hooks/web/useAxios'
import type { DeptVO, DeptListReqVO } from './types'

const request = useAxios()

// 查询部门（精简)列表
export const listSimpleDeptApi = () => {
  return request.get({ url: '/system/dept/list-all-simple' })
}

// 查询部门列表
export const getDeptPageApi = (params: DeptListReqVO) => {
  return request.get({ url: '/system/dept/list', params })
}

// 查询部门详情
export const getDeptApi = (id: number) => {
  return request.get({ url: '/system/dept/get?id=' + id })
}

// 新增部门
export const createDeptApi = (data: DeptVO) => {
  return request.post({ url: '/system/dept/create', data: data })
}

// 修改部门
export const updateDeptApi = (params: DeptVO) => {
  return request.put({ url: '/system/dept/update', data: params })
}

// 删除部门
export const deleteDeptApi = (id: number) => {
  return request.delete({ url: '/system/dept/delete?id=' + id })
}
