import { defHttp } from '@/config/axios'
import type { DictDataVO } from './types'

// 查询字典数据（精简)列表
export const listSimpleDictDataApi = () => {
  return defHttp.get({ url: '/system/dict-data/list-all-simple' })
}

// 查询字典数据列表
export const getDictDataPageApi = ({ params }) => {
  return defHttp.get<PageResult<DictDataVO>>({ url: '/system/dict-data/page', params })
}

// 查询字典数据详情
export const getDictDataApi = (id: number) => {
  return defHttp.get({ url: '/system/dict-data/get?id=' + id })
}

// 新增字典数据
export const createDictDataApi = (params: DictDataVO) => {
  return defHttp.post({ url: '/system/dict-data/create', params })
}

// 修改字典数据
export const updateDictDataApi = (params: DictDataVO) => {
  return defHttp.put({ url: '/system/dict-data/update', params })
}

// 删除字典数据
export const deleteDictDataApi = (id: number) => {
  return defHttp.delete({ url: '/system/dict-data/delete?id=' + id })
}
// 导出字典类型数据
export const exportDictDataApi = (params: DictDataVO) => {
  return defHttp.get({ url: '/system/dict-data/export', params })
}
