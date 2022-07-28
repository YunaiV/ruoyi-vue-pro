import { useAxios } from '@/hooks/web/useAxios'
import { ModelVO } from './types'
const request = useAxios()

export const getModelPage = async (params) => {
  return await request.get({ url: '/bpm/model/page', params })
}

export const getModel = async (id: number) => {
  return await request.get({ url: '/bpm/model/get?id=' + id })
}

export const updateModel = async (data: ModelVO) => {
  return await request.put({ url: '/bpm/model/update', data: data })
}

// 任务状态修改
export const updateModelState = async (id: number, state: string) => {
  const data = {
    id: id,
    state: state
  }
  return await request.put({ url: '/bpm/model/update-state', data: data })
}

export const createModel = async (data: ModelVO) => {
  return await request.post({ url: '/bpm/model/create', data: data })
}

export const deleteModel = async (id: number) => {
  return await request.delete({ url: '/bpm/model/delete?id=' + id })
}

export const deployModel = async (id: number) => {
  return await request.post({ url: '/bpm/model/deploy?id=' + id })
}
