import type { ComponentStyle, DiyComponent } from '../../../util';

/** 用户订单属性 */
export interface UserOrderProperty {
  style: ComponentStyle; // 组件样式
}

/** 定义组件 */
export const component = {
  id: 'UserOrder',
  name: '用户订单',
  icon: 'lucide:clipboard-list',
  property: {
    style: {
      bgType: 'color',
      bgColor: '',
      marginLeft: 8,
      marginRight: 8,
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<UserOrderProperty>;
