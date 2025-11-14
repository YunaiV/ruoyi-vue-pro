import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

import { cloneDeep } from '@vben/utils';

/** 菜单导航属性 */
export interface MenuSwiperProperty {
  // 布局： 图标+文字 | 图标
  layout: 'icon' | 'iconText';
  // 行数
  row: number;
  // 列数
  column: number;
  // 导航菜单列表
  list: MenuSwiperItemProperty[];
  // 组件样式
  style: ComponentStyle;
}
/** 菜单导航项目属性 */
export interface MenuSwiperItemProperty {
  // 图标链接
  iconUrl: string;
  // 标题
  title: string;
  // 标题颜色
  titleColor: string;
  // 链接
  url: string;
  // 角标
  badge: {
    // 角标背景颜色
    bgColor: string;
    // 是否显示
    show: boolean;
    // 角标文字
    text: string;
    // 角标文字颜色
    textColor: string;
  };
}

export const EMPTY_MENU_SWIPER_ITEM_PROPERTY = {
  title: '标题',
  titleColor: '#333',
  badge: {
    show: false,
    textColor: '#fff',
    bgColor: '#FF6000',
  },
} as MenuSwiperItemProperty;

// 定义组件
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
