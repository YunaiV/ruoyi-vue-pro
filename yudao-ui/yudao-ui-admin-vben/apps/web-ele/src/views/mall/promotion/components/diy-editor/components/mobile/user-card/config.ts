import type { ComponentStyle, DiyComponent } from '../../../util';

/** 用户卡片属性 */
export interface UserCardProperty {
  style: ComponentStyle; // 组件样式
}

/** 定义组件 */
export const component = {
  id: 'UserCard',
  name: '用户卡片',
  icon: 'mdi:user-card-details',
  property: {
    style: {
      bgType: 'color',
      bgColor: '',
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<UserCardProperty>;
