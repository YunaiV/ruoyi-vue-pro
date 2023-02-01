import request from '@/utils/request'

// 获得公众号菜单列表
export function getMenuList(accountId) {
  return request({
    url: '/mp/menu/list?accountId=' + accountId,
    method: 'get',
  })
}

// 保存公众号菜单
export function saveMenu(accountId, menus) {
  return request({
    url: '/mp/menu/save',
    method: 'post',
    data: {
      accountId,
      menus
    }
  })
}

// 删除公众号菜单
export function deleteMenu(accountId) {
  return request({
    url: '/mp/menu/delete?accountId=' + accountId,
    method: 'delete',
  })
}
