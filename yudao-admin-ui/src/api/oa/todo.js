import request from '@/utils/request'

// 创建请假申请
export function createLeave(data) {
  return request({
    url: '/oa/leave/create',
    method: 'post',
    data: data
  })
}

// 更新请假申请
export function updateLeave(data) {
  return request({
    url: '/oa/leave/update',
    method: 'put',
    data: data
  })
}

// 删除请假申请
export function deleteLeave(id) {
  return request({
    url: '/oa/leave/delete?id=' + id,
    method: 'delete'
  })
}

// 获得请假申请
export function getLeave(id) {
  return request({
    url: '/oa/leave/get?id=' + id,
    method: 'get'
  })
}

// 获得待办任务分页
export function getTodoTaskPage(query) {
  return request({
    url: '/workflow/task/todo/page',
    method: 'get',
    params: query
  })
}

// 签收任务
export function claimTask(id) {
  return request({
    url: '/workflow/task/claim?id=' + id,
    method: 'get'
  })
}

export function completeTask(data) {
  return request({
    url: '/workflow/task/complete',
    method: 'post',
    data: data
  })
}

export function taskSteps(data) {
  return request({
    url: '/workflow/task/task-steps',
    method: 'post',
    data: data
  })
}

export function getTaskFormKey(data) {
  return request({
    url: '/workflow/task/formKey',
    method: 'post',
    data: data
  })
}

export function processHistorySteps(id) {
  return request({
    url: '/workflow/task/process/history-steps?id='+id,
    method: 'get'
  })
}
