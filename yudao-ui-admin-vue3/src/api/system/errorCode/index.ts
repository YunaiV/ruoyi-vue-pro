import { useAxios } from '@/hooks/web/useAxios'
import type { ErrorCodeVO } from './types'

const request = useAxios()

// 查询错误码列表
export const getErrorCodePageApi = (params) => {
  return request.get({ url: '/system/error-code/page', params })
}

// 查询错误码详情
export const getErrorCodeApi = (id: number) => {
  return request.get({ url: '/system/error-code/get?id=' + id })
}

// 新增错误码
export const createErrorCodeApi = (data: ErrorCodeVO) => {
  return request.post({ url: '/system/error-code/create', data })
}

// 修改错误码
export const updateErrorCodeApi = (data: ErrorCodeVO) => {
  return request.put({ url: '/system/error-code/update', data })
}

// 删除错误码
export const deleteErrorCodeApi = (id: number) => {
  return request.delete({ url: '/system/error-code/delete?id=' + id })
}
// 导出错误码
export const excelErrorCodeApi = (params) => {
  return request.download({ url: '/system/error-code/export-excel', params })
}
