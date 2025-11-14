import { requestClient } from '#/api/request';

/** 菜单类型枚举 */
export enum MenuType {
  CLICK = 'click', // 点击推事件
  LOCATION_SELECT = 'location_select', // 发送位置
  MEDIA_ID = 'media_id', // 下发消息
  MINIPROGRAM = 'miniprogram', // 小程序
  PIC_PHOTO_OR_ALBUM = 'pic_photo_or_album', // 拍照或者相册发图
  PIC_SYSPHOTO = 'pic_sysphoto', // 系统拍照发图
  PIC_WEIXIN = 'pic_weixin', // 微信相册发图
  SCANCODE_PUSH = 'scancode_push', // 扫码推事件
  SCANCODE_WAITMSG = 'scancode_waitmsg', // 扫码带提示
  VIEW = 'view', // 跳转URL
  VIEW_LIMITED = 'view_limited', // 跳转图文消息URL
}

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
