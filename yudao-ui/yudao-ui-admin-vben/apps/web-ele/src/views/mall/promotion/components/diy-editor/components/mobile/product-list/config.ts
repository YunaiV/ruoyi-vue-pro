import type { ComponentStyle, DiyComponent } from '../../../util';

/** 商品栏属性 */
export interface ProductListProperty {
  layoutType: 'horizSwiper' | 'threeCol' | 'twoCol'; // 布局类型：双列 | 三列 | 水平滑动
  fields: {
    name: ProductListFieldProperty; // 商品名称
    price: ProductListFieldProperty; // 商品价格
  }; // 商品字段
  badge: {
    imgUrl: string; // 角标图片
    show: boolean; // 是否显示
  }; // 角标
  borderRadiusTop: number; // 上圆角
  borderRadiusBottom: number; // 下圆角
  space: number; // 间距
  spuIds: number[]; // 商品编号列表
  style: ComponentStyle; // 组件样式
}

/** 商品字段属性 */
export interface ProductListFieldProperty {
  show: boolean; // 是否显示
  color: string; // 颜色
}

/** 定义组件 */
export const component = {
  id: 'ProductList',
  name: '商品栏',
  icon: 'fluent:text-column-two-24-filled',
  property: {
    layoutType: 'twoCol',
    fields: {
      name: { show: true, color: '#000' },
      price: { show: true, color: '#ff3000' },
    },
    badge: { show: false, imgUrl: '' },
    borderRadiusTop: 8,
    borderRadiusBottom: 8,
    space: 8,
    spuIds: [],
    style: {
      bgType: 'color',
      bgColor: '',
      marginLeft: 8,
      marginRight: 8,
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<ProductListProperty>;
