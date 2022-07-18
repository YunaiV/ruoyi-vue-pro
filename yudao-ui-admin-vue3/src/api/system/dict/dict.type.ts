import { defHttp } from '@/config/axios'
import type { DictTypeVO } from './types'

// 查询字典（精简)列表
export const listSimpleDictTypeApi = () => {
  return defHttp.get({ url: '/system/dict-type/list-all-simple' })
}

// 查询字典列表
export const getDictTypePageApi = ({ params }) => {
  return defHttp.get<PageResult<DictTypeVO>>({ url: '/system/dict-type/page', params })
}

// 查询字典详情
export const getDictTypeApi = (id: number) => {
  return defHttp.get({ url: '/system/dict-type/get?id=' + id })
}

// 新增字典
export const createDictTypeApi = (params: DictTypeVO) => {
  return defHttp.post({ url: '/system/dict-type/create', params })
}

// 修改字典
export const updateDictTypeApi = (params: DictTypeVO) => {
  return defHttp.put({ url: '/system/dict-type/update', params })
}

// 删除字典
export const deleteDictTypeApi = (id: number) => {
  return defHttp.delete({ url: '/system/dict-type/delete?id=' + id })
}
// 导出字典类型
export const exportDictTypeApi = (params: DictTypeVO) => {
  return defHttp.get({ url: '/system/dict-type/export', params })
}
