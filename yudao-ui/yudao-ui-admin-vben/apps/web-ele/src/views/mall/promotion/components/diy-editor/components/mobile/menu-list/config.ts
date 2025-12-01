import type { ComponentStyle, DiyComponent } from '../../../util';

import { cloneDeep } from '@vben/utils';

/** 列表导航属性 */
export interface MenuListProperty {
  list: MenuListItemProperty[]; // 导航菜单列表
  style: ComponentStyle; // 组件样式
}

/** 列表导航项目属性 */
export interface MenuListItemProperty {
  iconUrl: string; // 图标链接
  title: string; // 标题
  titleColor: string; // 标题颜色
  subtitle: string; // 副标题
  subtitleColor: string; // 副标题颜色
  url: string; // 链接
}

/** 空的列表导航项目属性 */
export const EMPTY_MENU_LIST_ITEM_PROPERTY = {
  title: '标题',
  titleColor: '#333',
  subtitle: '副标题',
  subtitleColor: '#bbb',
};

/** 定义组件 */
export const component = {
  id: 'MenuList',
  name: '列表导航',
  icon: 'fa-solid:list',
  property: {
    list: [cloneDeep(EMPTY_MENU_LIST_ITEM_PROPERTY)],
    style: {
      bgType: 'color',
      bgColor: '#fff',
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<MenuListProperty>;
