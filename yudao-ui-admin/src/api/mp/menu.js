import request from '@/utils/request'

// 获得公众号菜单列表
export function getMenuList(accountId) {
  return request({
    url: '/mp/menu/list?accountId=' + accountId,
    method: 'get',
  })
}
