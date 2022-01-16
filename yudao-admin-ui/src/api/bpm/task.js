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

export function getHistoricTaskListByProcessInstanceId(processInstanceId) {
  return request({
    url: '/bpm/task/historic-list-by-process-instance-id?processInstanceId=' + processInstanceId,
    method: 'get',
  })
}
