import request from '@/config/axios'

export type FileVO = {
  id: number
  path: string
  url: string
  size: string
  type: string
  createTime: string
}

export interface FilePageReqVO extends PageParam {
  name?: string
  createTime?: string[]
}

// 查询文件列表
export const getFilePageApi = (params: FilePageReqVO) => {
  return request.get({ url: '/infra/file/page', params })
}

// 删除文件
export const deleteFileApi = (id: number) => {
  return request.delete({ url: '/infra/file/delete?id=' + id })
}
