import { useAxios } from '@/hooks/web/useAxios'
import { LeaveVO } from './types'
const request = useAxios()

// 创建请假申请
export const createLeaveApi = async (data: LeaveVO) => {
  return await request.post({ url: '/bpm/oa/leave/create', data: data })
}

// 获得请假申请
export const getLeaveApi = async (id: number) => {
  return await request.get({ url: '/bpm/oa/leave/get?id=' + id })
}

// 获得请假申请分页
export const getLeavePageApi = async (params) => {
  return await request.get({ url: '/bpm/oa/leave/page', params })
}
