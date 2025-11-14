import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

/** 商品栏属性 */
export interface ProductListProperty {
  // 布局类型：双列 | 三列 | 水平滑动
  layoutType: 'horizSwiper' | 'threeCol' | 'twoCol';
  // 商品字段
  fields: {
    // 商品名称
    name: ProductListFieldProperty;
    // 商品价格
    price: ProductListFieldProperty;
  };
  // 角标
  badge: {
    // 角标图片
    imgUrl: string;
    // 是否显示
    show: boolean;
  };
  // 上圆角
  borderRadiusTop: number;
  // 下圆角
  borderRadiusBottom: number;
  // 间距
  space: number;
  // 商品编号列表
  spuIds: number[];
  // 组件样式
  style: ComponentStyle;
}
// 商品字段
export interface ProductListFieldProperty {
  // 是否显示
  show: boolean;
  // 颜色
  color: string;
}

// 定义组件
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
