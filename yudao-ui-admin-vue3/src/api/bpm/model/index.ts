import { useAxios } from '@/hooks/web/useAxios'
import { ModelVO } from './types'
const request = useAxios()

export const getModelPageApi = async (params) => {
  return await request.get({ url: '/bpm/model/page', params })
}

export const getModelApi = async (id: number) => {
  return await request.get({ url: '/bpm/model/get?id=' + id })
}

export const updateModelApi = async (data: ModelVO) => {
  return await request.put({ url: '/bpm/model/update', data: data })
}

// 任务状态修改
export const updateModelStateApi = async (id: number, state: number) => {
  const data = {
    id: id,
    state: state
  }
  return await request.put({ url: '/bpm/model/update-state', data: data })
}

export const createModelApi = async (data: ModelVO) => {
  return await request.post({ url: '/bpm/model/create', data: data })
}

export const deleteModelApi = async (id: number) => {
  return await request.delete({ url: '/bpm/model/delete?id=' + id })
}

export const deployModelApi = async (id: number) => {
  return await request.post({ url: '/bpm/model/deploy?id=' + id })
}
