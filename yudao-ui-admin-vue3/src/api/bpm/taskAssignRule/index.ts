import request from '@/config/axios'
import { TaskAssignVO } from './types'

export const getTaskAssignRuleList = async (params) => {
  return await request.get({ url: '/bpm/task-assign-rule/list', params })
}

export const createTaskAssignRule = async (data: TaskAssignVO) => {
  return await request.post({
    url: '/bpm/task-assign-rule/create',
    data: data
  })
}

export const updateTaskAssignRule = async (data: TaskAssignVO) => {
  return await request.put({
    url: '/bpm/task-assign-rule/update',
    data: data
  })
}
