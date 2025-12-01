import type { ComponentStyle, DiyComponent } from '../../../util';

/** 图片展示属性 */
export interface ImageBarProperty {
  imgUrl: string; // 图片链接
  url: string; // 跳转链接
  style: ComponentStyle; // 组件样式
}

/** 定义组件 */
export const component = {
  id: 'ImageBar',
  name: '图片展示',
  icon: 'lucide:image',
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
