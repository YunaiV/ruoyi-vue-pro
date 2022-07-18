import { defHttp } from '@/config/axios'
import type { SensitiveWordVO } from './types'

// 查询敏感词列表
export const getSensitiveWordPageApi = ({ params }) => {
  return defHttp.get<PageResult<SensitiveWordVO>>({ url: '/system/sensitive-word/page', params })
}

// 查询敏感词详情
export const getSensitiveWordApi = (id: number) => {
  return defHttp.get<SensitiveWordVO>({ url: '/system/sensitive-word/get?id=' + id })
}

// 新增敏感词
export const createSensitiveWordApi = (params: SensitiveWordVO) => {
  return defHttp.post({ url: '/system/sensitive-word/create', params })
}

// 修改敏感词
export const updateSensitiveWordApi = (params: SensitiveWordVO) => {
  return defHttp.put({ url: '/system/sensitive-word/update', params })
}

// 删除敏感词
export const deleteSensitiveWordApi = (id: number) => {
  return defHttp.delete({ url: '/system/sensitive-word/delete?id=' + id })
}

// 导出敏感词
export const exportSensitiveWordApi = (params) => {
  return defHttp.get({ url: '/system/sensitive-word/export', params, responseType: 'blob' })
}

// 获取所有敏感词的标签数组
export const getSensitiveWordTagsApi = () => {
  return defHttp.get<SensitiveWordVO>({ url: '/system/sensitive-word/get-tags' })
}

// 获得文本所包含的不合法的敏感词数组
export const validateTextApi = (id: number) => {
  return defHttp.get<SensitiveWordVO>({ url: '/system/sensitive-word/validate-text?' + id })
}
