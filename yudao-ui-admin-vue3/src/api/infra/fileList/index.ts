import request from '@/config/axios'

export interface FileVO {
  id: number
  configId: number
  path: string
  name: string
  url: string
  size: string
  type: string
  createTime: Date
}

export interface FilePageReqVO extends PageParam {
  path?: string
  type?: string
  createTime?: Date[]
}

// 查询文件列表
export const getFilePageApi = (params: FilePageReqVO) => {
  return request.get({ url: '/infra/file/page', params })
}

// 删除文件
export const deleteFileApi = (id: number) => {
  return request.delete({ url: '/infra/file/delete?id=' + id })
}
