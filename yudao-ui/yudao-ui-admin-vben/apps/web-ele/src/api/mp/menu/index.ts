import { MenuType } from '@vben/constants';

import { requestClient } from '#/api/request';

export namespace MpMenuApi {
  /** 菜单按钮信息 */
  export interface MenuButton {
    type: MenuType;
    name: string;
    key?: string;
    url?: string;
    mediaId?: string;
    appId?: string;
    pagePath?: string;
    subButtons?: MenuButton[];
  }

  /** 菜单信息 */
  export interface Menu {
    accountId: number;
    menus: MenuButton[];
  }
}

/** 查询菜单列表 */
export function getMenuList(accountId: number) {
  return requestClient.get<MpMenuApi.MenuButton[]>('/mp/menu/list', {
    params: { accountId },
  });
}

/** 保存菜单 */
export function saveMenu(accountId: number, menus: MpMenuApi.MenuButton[]) {
  return requestClient.post('/mp/menu/save', {
    accountId,
    menus,
  });
}

/** 删除菜单 */
export function deleteMenu(accountId: number) {
  return requestClient.delete('/mp/menu/delete', {
    params: { accountId },
  });
}
