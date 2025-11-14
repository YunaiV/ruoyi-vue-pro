import type { DiyComponent } from '#/components/diy-editor/util';

// 悬浮按钮属性
export interface FloatingActionButtonProperty {
  // 展开方向
  direction: 'horizontal' | 'vertical';
  // 是否显示文字
  showText: boolean;
  // 按钮列表
  list: FloatingActionButtonItemProperty[];
}

// 悬浮按钮项属性
export interface FloatingActionButtonItemProperty {
  // 图片地址
  imgUrl: string;
  // 跳转连接
  url: string;
  // 文字
  text: string;
  // 文字颜色
  textColor: string;
}

// 定义组件
export const component = {
  id: 'FloatingActionButton',
  name: '悬浮按钮',
  icon: 'tabler:float-right',
  position: 'fixed',
  property: {
    direction: 'vertical',
    showText: true,
    list: [{ textColor: '#fff' }],
  },
} as DiyComponent<FloatingActionButtonProperty>;
