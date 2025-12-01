import type { ComponentStyle, DiyComponent } from '../../../util';

import { cloneDeep } from '@vben/utils';

/** 菜单导航属性 */
export interface MenuSwiperProperty {
  layout: 'icon' | 'iconText'; // 布局：图标+文字 | 图标
  row: number; // 行数
  column: number; // 列数
  list: MenuSwiperItemProperty[]; // 导航菜单列表
  style: ComponentStyle; // 组件样式
}

/** 菜单导航项目属性 */
export interface MenuSwiperItemProperty {
  iconUrl: string; // 图标链接
  title: string; // 标题
  titleColor: string; // 标题颜色
  url: string; // 链接
  badge: {
    bgColor: string; // 角标背景颜色
    show: boolean; // 是否显示
    text: string; // 角标文字
    textColor: string; // 角标文字颜色
  }; // 角标
}

/** 空菜单导航项目属性 */
export const EMPTY_MENU_SWIPER_ITEM_PROPERTY = {
  title: '标题',
  titleColor: '#333',
  badge: {
    show: false,
    textColor: '#fff',
    bgColor: '#FF6000',
  },
} as MenuSwiperItemProperty;

/** 定义组件 */
export const component = {
  id: 'MenuSwiper',
  name: '菜单导航',
  icon: 'bi:grid-3x2-gap',
  property: {
    layout: 'iconText',
    row: 1,
    column: 3,
    list: [cloneDeep(EMPTY_MENU_SWIPER_ITEM_PROPERTY)],
    style: {
      bgType: 'color',
      bgColor: '#fff',
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<MenuSwiperProperty>;
