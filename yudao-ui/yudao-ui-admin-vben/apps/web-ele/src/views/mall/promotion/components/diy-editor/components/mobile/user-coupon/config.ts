import type { ComponentStyle, DiyComponent } from '../../../util';

/** 用户卡券属性 */
export interface UserCouponProperty {
  style: ComponentStyle; // 组件样式
}

/** 定义组件 */
export const component = {
  id: 'UserCoupon',
  name: '用户卡券',
  icon: 'lucide:ticket',
  property: {
    style: {
      bgType: 'color',
      bgColor: '',
      marginLeft: 8,
      marginRight: 8,
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<UserCouponProperty>;
