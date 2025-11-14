import type { Component, Ref } from 'vue';

import type { MenuRecordBadgeRaw, ThemeModeType } from '@vben-core/typings';

interface MenuProps {
  /**
   * @zh_CN 是否开启手风琴模式
   * @default true
   */
  accordion?: boolean;
  /**
   * @zh_CN 菜单是否折叠
   * @default false
   */
  collapse?: boolean;

  /**
   * @zh_CN 菜单折叠时是否显示菜单名称
   * @default false
   */
  collapseShowTitle?: boolean;

  /**
   * @zh_CN 默认激活的菜单
   */
  defaultActive?: string;

  /**
   * @zh_CN 默认展开的菜单
   */
  defaultOpeneds?: string[];

  /**
   * @zh_CN 菜单模式
   * @default vertical
   */
  mode?: 'horizontal' | 'vertical';

  /**
   * @zh_CN 是否圆润风格
   * @default true
   */
  rounded?: boolean;

  /**
   * @zh_CN 是否自动滚动到激活的菜单项
   * @default false
   */
  scrollToActive?: boolean;

  /**
   * @zh_CN 菜单主题
   * @default dark
   */
  theme?: ThemeModeType;
}

interface SubMenuProps extends MenuRecordBadgeRaw {
  /**
   * @zh_CN 激活图标
   */
  activeIcon?: string;
  /**
   * @zh_CN 是否禁用
   */
  disabled?: boolean;
  /**
   * @zh_CN 图标
   */
  icon?: Component | string;
  /**
   * @zh_CN submenu 名称
   */
  path: string;
}

interface MenuItemProps extends MenuRecordBadgeRaw {
  /**
   * @zh_CN 图标
   */
  activeIcon?: string;
  /**
   * @zh_CN 是否禁用
   */
  disabled?: boolean;
  /**
   * @zh_CN 图标
   */
  icon?: Component | string;
  /**
   * @zh_CN menuitem 名称
   */
  path: string;
}

interface MenuItemRegistered {
  active: boolean;
  parentPaths: string[];
  path: string;
}

interface MenuItemClicked {
  parentPaths: string[];
  path: string;
}

interface MenuProvider {
  activePath?: string;
  addMenuItem: (item: MenuItemRegistered) => void;

  addSubMenu: (item: MenuItemRegistered) => void;
  closeMenu: (path: string, parentLinks: string[]) => void;
  handleMenuItemClick: (item: MenuItemClicked) => void;
  handleSubMenuClick: (subMenu: MenuItemRegistered) => void;
  isMenuPopup: boolean;
  items: Record<string, MenuItemRegistered>;

  openedMenus: string[];
  openMenu: (path: string, parentLinks: string[]) => void;
  props: MenuProps;
  removeMenuItem: (item: MenuItemRegistered) => void;

  removeSubMenu: (item: MenuItemRegistered) => void;

  subMenus: Record<string, MenuItemRegistered>;
  theme: string;
}

interface SubMenuProvider {
  addSubMenu: (item: MenuItemRegistered) => void;
  handleMouseleave?: (deepDispatch: boolean) => void;
  level: number;
  mouseInChild: Ref<boolean>;
  removeSubMenu: (item: MenuItemRegistered) => void;
}

export type {
  MenuItemClicked,
  MenuItemProps,
  MenuItemRegistered,
  MenuProps,
  MenuProvider,
  SubMenuProps,
  SubMenuProvider,
};
