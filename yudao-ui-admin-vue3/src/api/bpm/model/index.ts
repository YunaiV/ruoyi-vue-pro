import request from '@/config/axios'

export type ProcessDefinitionVO = {
  id: string
  version: number
  deploymentTIme: string
  suspensionState: number
}

export type ModelVO = {
  id: number
  formName: string
  key: string
  name: string
  description: string
  category: string
  formType: number
  formId: number
  formCustomCreatePath: string
  formCustomViewPath: string
  processDefinition: ProcessDefinitionVO
  status: number
  remark: string
  createTime: string
}

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
