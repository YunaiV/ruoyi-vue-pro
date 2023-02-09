import request from '@/config/axios'

// 获得公众号菜单列表
export const getMenuList = (accountId) => {
  return request.get({
    url: '/mp/menu/list?accountId=' + accountId
  })
}

// 保存公众号菜单
export const saveMenu = (accountId, menus) => {
  return request.post({
    url: '/mp/menu/save',
    data: {
      accountId,
      menus
    }
  })
}

// 删除公众号菜单
export const deleteMenu = (accountId) => {
  return request.delete({
    url: '/mp/menu/delete?accountId=' + accountId
  })
}
