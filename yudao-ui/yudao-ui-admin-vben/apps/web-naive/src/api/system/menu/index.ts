import { requestClient } from '#/api/request';

export namespace SystemMenuApi {
  /** 菜单信息 */
  export interface Menu {
    id: number;
    name: string;
    permission: string;
    type: number;
    sort: number;
    parentId: number;
    path: string;
    icon: string;
    component: string;
    componentName?: string;
    status: number;
    visible: boolean;
    keepAlive: boolean;
    alwaysShow?: boolean;
    createTime: Date;
  }
}

/** 查询菜单（精简）列表 */
export async function getSimpleMenusList() {
  return requestClient.get<SystemMenuApi.Menu[]>('/system/menu/simple-list');
}

/** 查询菜单列表 */
export async function getMenuList(params?: Record<string, any>) {
  return requestClient.get<SystemMenuApi.Menu[]>('/system/menu/list', {
    params,
  });
}

/** 获取菜单详情 */
export async function getMenu(id: number) {
  return requestClient.get<SystemMenuApi.Menu>(`/system/menu/get?id=${id}`);
}

/** 新增菜单 */
export async function createMenu(data: SystemMenuApi.Menu) {
  return requestClient.post('/system/menu/create', data);
}

/** 修改菜单 */
export async function updateMenu(data: SystemMenuApi.Menu) {
  return requestClient.put('/system/menu/update', data);
}

/** 删除菜单 */
export async function deleteMenu(id: number) {
  return requestClient.delete(`/system/menu/delete?id=${id}`);
}

/** 批量删除菜单 */
export async function deleteMenuList(ids: number[]) {
  return requestClient.delete(`/system/menu/delete-list?ids=${ids.join(',')}`);
}
