import { useAxios } from '@/hooks/web/useAxios'
import { ProcessInstanceVO } from './types'
const request = useAxios()

export const getMyProcessInstancePage = async (params) => {
  return await request.get({ url: '/bpm/process-instance/my-page', params })
}

export const createProcessInstance = async (data: ProcessInstanceVO) => {
  return await request.post({ url: '/bpm/process-instance/create', data: data })
}

export const cancelProcessInstance = async (id: number, reason: string) => {
  const data = {
    id: id,
    reason: reason
  }
  return await request.delete({ url: '/bpm/process-instance/cancel', data: data })
}

export const getProcessInstance = async (id: number) => {
  return await request.get({ url: '/bpm/process-instance/get?id=' + id })
}
