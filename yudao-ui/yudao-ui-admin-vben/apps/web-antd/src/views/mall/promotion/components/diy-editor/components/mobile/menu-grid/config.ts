import type { ComponentStyle, DiyComponent } from '../../../util';

import { cloneDeep } from '@vben/utils';

/** 宫格导航属性 */
export interface MenuGridProperty {
  column: number; // 列数
  list: MenuGridItemProperty[]; // 导航菜单列表
  style: ComponentStyle; // 组件样式
}

/** 宫格导航项目属性 */
export interface MenuGridItemProperty {
  iconUrl: string; // 图标链接
  title: string; // 标题
  titleColor: string; // 标题颜色
  subtitle: string; // 副标题
  subtitleColor: string; // 副标题颜色
  url: string; // 链接
  badge: {
    bgColor: string; // 角标背景颜色
    show: boolean; // 是否显示
    text: string; // 角标文字
    textColor: string; // 角标文字颜色
  };
}

/** 宫格导航项目默认属性 */
export const EMPTY_MENU_GRID_ITEM_PROPERTY = {
  title: '标题',
  titleColor: '#333',
  subtitle: '副标题',
  subtitleColor: '#bbb',
  badge: {
    show: false,
    textColor: '#fff',
    bgColor: '#FF6000',
  },
} as MenuGridItemProperty;

/** 定义组件 */
export const component = {
  id: 'MenuGrid',
  name: '宫格导航',
  icon: 'bi:grid-3x3-gap',
  property: {
    column: 3,
    list: [cloneDeep(EMPTY_MENU_GRID_ITEM_PROPERTY)],
    style: {
      bgType: 'color',
      bgColor: '#fff',
      marginBottom: 8,
      marginLeft: 8,
      marginRight: 8,
      padding: 8,
      paddingTop: 8,
      paddingRight: 8,
      paddingBottom: 8,
      paddingLeft: 8,
      borderRadius: 8,
      borderTopLeftRadius: 8,
      borderTopRightRadius: 8,
      borderBottomRightRadius: 8,
      borderBottomLeftRadius: 8,
    } as ComponentStyle,
  },
} as DiyComponent<MenuGridProperty>;
