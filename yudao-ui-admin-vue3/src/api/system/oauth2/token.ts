import { useAxios } from '@/hooks/web/useAxios'

const request = useAxios()

// 查询 token列表
export const getAccessTokenPageApi = (params) => {
  return request.get({ url: '/system/oauth2-token/page', params })
}

// 删除 token
export const deleteAccessTokenApi = (accessToken: number) => {
  return request.delete({ url: '/system/oauth2-token/delete?accessToken=' + accessToken })
}
