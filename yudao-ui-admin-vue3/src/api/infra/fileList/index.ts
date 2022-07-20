import { useAxios } from '@/hooks/web/useAxios'

const request = useAxios()

// 查询文件列表
export const getFilePageApi = (params) => {
  return request.get({ url: '/infra/file/page', params })
}

// 删除文件
export const deleteFileApi = (id: number) => {
  return request.delete({ url: '/infra/file/delete?id=' + id })
}
