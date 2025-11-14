import type { AppRouteRecordRaw, BasicUserInfo } from '@vben-core/typings';

/** 用户信息 */
interface UserInfo extends BasicUserInfo {
  /**
   * 首页地址
   */
  homePath: string;
}

/** 权限信息 */
interface AuthPermissionInfo {
  user: UserInfo;
  roles: string[];
  permissions: string[];
  menus: AppRouteRecordRaw[];
}

export type { AuthPermissionInfo, UserInfo };
