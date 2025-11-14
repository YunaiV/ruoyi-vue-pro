import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

/** 轮播图属性 */
export interface CarouselProperty {
  // 类型：默认 | 卡片
  type: 'card' | 'default';
  // 指示器样式：点 | 数字
  indicator: 'dot' | 'number';
  // 是否自动播放
  autoplay: boolean;
  // 播放间隔
  interval: number;
  // 轮播内容
  items: CarouselItemProperty[];
  // 组件样式
  style: ComponentStyle;
}
// 轮播内容属性
export interface CarouselItemProperty {
  // 类型：图片 | 视频
  type: 'img' | 'video';
  // 图片链接
  imgUrl: string;
  // 视频链接
  videoUrl: string;
  // 跳转链接
  url: string;
}

// 定义组件
export const component = {
  id: 'Carousel',
  name: '轮播图',
  icon: 'system-uicons:carousel',
  property: {
    type: 'default',
    indicator: 'dot',
    autoplay: false,
    interval: 3,
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
