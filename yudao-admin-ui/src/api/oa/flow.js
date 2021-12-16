import request from '@/utils/request'


export function getStartForm(processKey) {
  return request({
    url: '/workflow/process/definition/getStartForm?processKey='+processKey,
    method: 'get'
  })
}
