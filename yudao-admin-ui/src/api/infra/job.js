import request from '@/utils/request'

// 查询定时任务调度列表
export function listJob(query) {
  return request({
    url: '/infra/job/page',
    method: 'get',
    params: query
  })
}

// 查询定时任务调度详细
export function getJob(jobId) {
  return request({
    url: '/infra/job/get?id=' + jobId,
    method: 'get'
  })
}

// 新增定时任务调度
export function addJob(data) {
  return request({
    url: '/infra/job/create',
    method: 'post',
    data: data
  })
}

// 修改定时任务调度
export function updateJob(data) {
  return request({
    url: '/infra/job/update',
    method: 'put',
    data: data
  })
}

// 删除定时任务调度
export function delJob(jobId) {
  return request({
    url: '/infra/job/delete?id=' + jobId,
    method: 'delete'
  })
}

// 导出定时任务调度
export function exportJob(query) {
  return request({
    url: '/infra/job/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 任务状态修改
export function updateJobStatus(jobId, status) {
  return request({
    url: '/infra/job/update-status',
    method: 'put',
    headers:{
      'Content-type': 'application/x-www-form-urlencoded'
    },
    data: 'id=' + jobId + "&status=" + status,
  })
}

// 定时任务立即执行一次
export function runJob(jobId) {
  return request({
    url: '/infra/job/trigger?id=' + jobId,
    method: 'put'
  })
}

// 获得定时任务的下 n 次执行时间
export function getJobNextTimes(jobId) {
  return request({
    url: '/infra/job/get_next_times?id=' + jobId,
    method: 'get'
  })
}
