import request from '@/config/axios'
import { ProcessInstanceVO } from './types'

export const getMyProcessInstancePageApi = async (params) => {
  return await request.get({ url: '/bpm/process-instance/my-page', params })
}

export const createProcessInstanceApi = async (data: ProcessInstanceVO) => {
  return await request.post({ url: '/bpm/process-instance/create', data: data })
}

export const cancelProcessInstanceApi = async (id: number, reason: string) => {
  const data = {
    id: id,
    reason: reason
  }
  return await request.delete({ url: '/bpm/process-instance/cancel', data: data })
}

export const getProcessInstanceApi = async (id: number) => {
  return await request.get({ url: '/bpm/process-instance/get?id=' + id })
}
