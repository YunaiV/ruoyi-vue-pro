import type { ComponentStyle, DiyComponent } from '../../../util';

/** 用户资产属性 */
export interface UserWalletProperty {
  style: ComponentStyle; // 组件样式
}

/** 定义组件 */
export const component = {
  id: 'UserWallet',
  name: '用户资产',
  icon: 'lucide:wallet',
  property: {
    style: {
      bgType: 'color',
      bgColor: '',
      marginLeft: 8,
      marginRight: 8,
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<UserWalletProperty>;
