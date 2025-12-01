import type { ComponentStyle, DiyComponent } from '../../../util';

/** 优惠劵卡片属性 */
export interface CouponCardProperty {
  columns: number; // 列数
  bgImg: string; // 背景图
  textColor: string; // 文字颜色
  button: {
    bgColor: string; // 背景颜色
    color: string; // 文字颜色
  }; // 按钮样式
  space: number; // 间距
  couponIds: number[]; // 优惠券编号列表
  style: ComponentStyle; // 组件样式
}

/** 定义组件 */
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
