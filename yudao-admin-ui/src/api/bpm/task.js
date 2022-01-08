import request from '@/utils/request'

export function getTodoTaskPage(query) {
  return request({
    url: '/bpm/task/todo-page',
    method: 'get',
    params: query
  })
}

