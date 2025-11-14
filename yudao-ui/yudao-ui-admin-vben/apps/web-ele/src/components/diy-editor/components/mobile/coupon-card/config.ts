import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

/** 商品卡片属性 */
export interface CouponCardProperty {
  // 列数
  columns: number;
  // 背景图
  bgImg: string;
  // 文字颜色
  textColor: string;
  // 按钮样式
  button: {
    // 背景颜色
    bgColor: string;
    // 颜色
    color: string;
  };
  // 间距
  space: number;
  // 优惠券编号列表
  couponIds: number[];
  // 组件样式
  style: ComponentStyle;
}

// 定义组件
export const component = {
  id: 'CouponCard',
  name: '优惠券',
  icon: 'ep:ticket',
  property: {
    columns: 1,
    bgImg: '',
    textColor: '#E9B461',
    button: {
      color: '#434343',
      bgColor: '',
    },
    space: 0,
    couponIds: [],
    style: {
      bgType: 'color',
      bgColor: '',
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<CouponCardProperty>;
