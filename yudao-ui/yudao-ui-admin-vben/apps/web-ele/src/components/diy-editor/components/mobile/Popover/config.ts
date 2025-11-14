import type { DiyComponent } from '#/components/diy-editor/util';

/** 弹窗广告属性 */
export interface PopoverProperty {
  list: PopoverItemProperty[];
}

export interface PopoverItemProperty {
  // 图片地址
  imgUrl: string;
  // 跳转连接
  url: string;
  // 显示类型：仅显示一次、每次启动都会显示
  showType: 'always' | 'once';
}

// 定义组件
export const component = {
  id: 'Popover',
  name: '弹窗广告',
  icon: 'carbon:popup',
  position: 'fixed',
  property: {
    list: [{ showType: 'once' }],
  },
} as DiyComponent<PopoverProperty>;
