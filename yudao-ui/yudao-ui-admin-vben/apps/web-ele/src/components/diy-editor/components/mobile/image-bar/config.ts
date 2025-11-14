import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

/** 图片展示属性 */
export interface ImageBarProperty {
  // 图片链接
  imgUrl: string;
  // 跳转链接
  url: string;
  // 组件样式
  style: ComponentStyle;
}

// 定义组件
export const component = {
  id: 'ImageBar',
  name: '图片展示',
  icon: 'ep:picture',
  property: {
    imgUrl: '',
    url: '',
    style: {
      bgType: 'color',
      bgColor: '#fff',
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<ImageBarProperty>;
