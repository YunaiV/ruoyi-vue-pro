import { defHttp } from '@/config/axios'
import type { MenuVO } from './types'

// 查询菜单（精简)列表
export const listSimpleMenusApi = () => {
  return defHttp.get({ url: '/system/menu/list-all-simple' })
}
// 查询菜单列表
export const getMenuListApi = (params) => {
  return defHttp.get({ url: '/system/menu/list', params })
}

// 获取菜单详情
export const getMenuApi = (id: number) => {
  return defHttp.get<MenuVO>({ url: '/system/menu/get?id=' + id })
}

// 新增菜单
export const createMenuApi = (params: MenuVO) => {
  return defHttp.post({ url: '/system/menu/create', params })
}

// 修改菜单
export const updateMenuApi = (params: MenuVO) => {
  return defHttp.put({ url: '/system/menu/update', params })
}

// 删除菜单
export const deleteMenuApi = (id: number) => {
  return defHttp.delete({ url: '/system/menu/delete?id=' + id })
}
