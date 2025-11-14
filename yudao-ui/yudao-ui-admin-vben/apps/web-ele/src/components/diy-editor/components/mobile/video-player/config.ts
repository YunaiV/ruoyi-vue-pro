import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

/** 视频播放属性 */
export interface VideoPlayerProperty {
  // 视频链接
  videoUrl: string;
  // 封面链接
  posterUrl: string;
  // 是否自动播放
  autoplay: boolean;
  // 组件样式
  style: VideoPlayerStyle;
}

// 视频播放样式
export interface VideoPlayerStyle extends ComponentStyle {
  // 视频高度
  height: number;
}

// 定义组件
export const component = {
  id: 'VideoPlayer',
  name: '视频播放',
  icon: 'ep:video-play',
  property: {
    videoUrl: '',
    posterUrl: '',
    autoplay: false,
    style: {
      bgType: 'color',
      bgColor: '#fff',
      marginBottom: 8,
      height: 300,
    } as VideoPlayerStyle,
  },
} as DiyComponent<VideoPlayerProperty>;
