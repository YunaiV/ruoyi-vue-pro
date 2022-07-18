import { defHttp } from '@/config/axios'
import type { FileConfigVO } from './types'

// 查询文件配置列表
export const getFileConfigPageApi = ({ params }) => {
  return defHttp.get<PageResult<FileConfigVO>>({ url: '/infra/file-config/page', params })
}

// 查询文件配置详情
export const getFileConfigApi = (id: number) => {
  return defHttp.get<FileConfigVO>({ url: '/infra/file-config/get?id=' + id })
}

// 更新文件配置为主配置
export const updateFileConfigMasterApi = (id: number) => {
  return defHttp.get<FileConfigVO>({ url: '/infra/file-config/update-master?id=' + id })
}

// 新增文件配置
export const createFileConfigApi = (params: FileConfigVO) => {
  return defHttp.post({ url: '/infra/file-config/create', params })
}

// 修改文件配置
export const updateFileConfigApi = (params: FileConfigVO) => {
  return defHttp.put({ url: '/infra/file-config/update', params })
}

// 删除文件配置
export const deleteFileConfigApi = (id: number) => {
  return defHttp.delete({ url: '/infra/file-config/delete?id=' + id })
}

// 测试文件配置
export const testFileConfigApi = (id: number) => {
  return defHttp.get({ url: '/infra/file-config/test?id=' + id })
}
