import type { DiyComponent } from '../../../util';

/** 悬浮按钮属性 */
export interface FloatingActionButtonProperty {
  direction: 'horizontal' | 'vertical'; // 展开方向
  showText: boolean; // 是否显示文字
  list: FloatingActionButtonItemProperty[]; // 按钮列表
}

/** 悬浮按钮项属性 */
export interface FloatingActionButtonItemProperty {
  imgUrl: string; // 图片地址
  url: string; // 跳转连接
  text: string; // 文字
  textColor: string; // 文字颜色
}

/** 定义组件 */
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
