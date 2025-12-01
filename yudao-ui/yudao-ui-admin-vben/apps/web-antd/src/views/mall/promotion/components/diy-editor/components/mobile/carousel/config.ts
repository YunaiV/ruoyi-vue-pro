import type { ComponentStyle, DiyComponent } from '../../../util';

/** 轮播图属性 */
export interface CarouselProperty {
  type: 'card' | 'default'; // 类型：默认 | 卡片
  indicator: 'dot' | 'number'; // 指示器样式：点 | 数字
  autoplay: boolean; // 是否自动播放
  interval: number; // 播放间隔
  height: number; // 轮播高度
  items: CarouselItemProperty[]; // 轮播内容
  style: ComponentStyle; // 组件样式
}

/** 轮播内容属性 */
export interface CarouselItemProperty {
  type: 'img' | 'video'; // 类型：图片 | 视频
  imgUrl: string; // 图片链接
  videoUrl: string; // 视频链接
  url: string; // 跳转链接
}

/** 定义组件 */
export const component = {
  id: 'Carousel',
  name: '轮播图',
  icon: 'system-uicons:carousel',
  property: {
    type: 'default',
    indicator: 'dot',
    autoplay: false,
    interval: 3,
    height: 174,
    items: [
      {
        type: 'img',
        imgUrl: 'https://static.iocoder.cn/mall/banner-01.jpg',
        videoUrl: '',
      },
      {
        type: 'img',
        imgUrl: 'https://static.iocoder.cn/mall/banner-02.jpg',
        videoUrl: '',
      },
    ] as CarouselItemProperty[],
    style: {
      bgType: 'color',
      bgColor: '#fff',
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<CarouselProperty>;
