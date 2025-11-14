import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

/** 用户资产属性 */
export interface UserWalletProperty {
  // 组件样式
  style: ComponentStyle;
}

// 定义组件
export const component = {
  id: 'UserWallet',
  name: '用户资产',
  icon: 'ep:wallet-filled',
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
