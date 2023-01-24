import request from '@/config/axios'

export const getActivityList = async (params) => {
  return await request.get({
    url: '/bpm/activity/list',
    params
  })
}
