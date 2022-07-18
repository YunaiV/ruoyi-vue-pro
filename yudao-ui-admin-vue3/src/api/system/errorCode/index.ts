import { defHttp } from '@/config/axios'
import type { ErrorCodeVO } from './types'

// 查询错误码列表
export const getErrorCodePageApi = ({ params }) => {
  return defHttp.get<PageResult<ErrorCodeVO>>({ url: '/system/error-code/page', params })
}

// 查询错误码详情
export const getErrorCodeApi = (id: number) => {
  return defHttp.get<ErrorCodeVO>({ url: '/system/error-code/get?id=' + id })
}

// 新增错误码
export const createErrorCodeApi = (params: ErrorCodeVO) => {
  return defHttp.post({ url: '/system/error-code/create', params })
}

// 修改错误码
export const updateErrorCodeApi = (params: ErrorCodeVO) => {
  return defHttp.put({ url: '/system/error-code/update', params })
}

// 删除错误码
export const deleteErrorCodeApi = (id: number) => {
  return defHttp.delete({ url: '/system/error-code/delete?id=' + id })
}
// 导出错误码
export const excelErrorCodeApi = (params) => {
  return defHttp.get({ url: '/system/error-code/export-excel', params, responseType: 'blob' })
}
