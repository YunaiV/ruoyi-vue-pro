import { defHttp } from '@/config/axios'
import type { FileVO } from './types'

// 查询文件列表
export const getFilePageApi = ({ params }) => {
  return defHttp.get<PageResult<FileVO>>({ url: '/infra/file/page', params })
}

// 删除文件
export const deleteFileApi = (id: number) => {
  return defHttp.delete({ url: '/infra/file/delete?id=' + id })
}
