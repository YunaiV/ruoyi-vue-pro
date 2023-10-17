import request from '@/utils/request'

export function getTodoTaskPage(query) {
  return request({
    url: '/bpm/task/todo-page',
    method: 'get',
    params: query
  })
}

export function getDoneTaskPage(query) {
  return request({
    url: '/bpm/task/done-page',
    method: 'get',
    params: query
  })
}

export function completeTask(data) {
  return request({
    url: '/bpm/task/complete',
    method: 'PUT',
    data: data
  })
}

export function approveTask(data) {
  return request({
    url: '/bpm/task/approve',
    method: 'PUT',
    data: data
  })
}

export function rejectTask(data) {
  return request({
    url: '/bpm/task/reject',
    method: 'PUT',
    data: data
  })
}
export function backTask(data) {
  return request({
    url: '/bpm/task/back',
    method: 'PUT',
    data: data
  })
}

export function updateTaskAssignee(data) {
  return request({
    url: '/bpm/task/update-assignee',
    method: 'PUT',
    data: data
  })
}

export function getTaskListByProcessInstanceId(processInstanceId) {
  return request({
    url: '/bpm/task/list-by-process-instance-id?processInstanceId=' + processInstanceId,
    method: 'get',
  })
}
export function getReturnList(taskId) {
  return request({
    url: '/bpm/task/get-return-list?taskId='+ taskId,
    method: 'get',
  })
}


export function returnTask(data) {
  return request({
    url: '/bpm/task/return',
    method: 'PUT',
    data: data
  })
}
export function delegateTask(data) {
  return request({
    url: '/bpm/task/delegate',
    method: 'PUT',
    data: data
  })
}
