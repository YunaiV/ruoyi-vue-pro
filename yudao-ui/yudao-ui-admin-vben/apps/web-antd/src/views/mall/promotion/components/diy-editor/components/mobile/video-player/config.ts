import type { ComponentStyle, DiyComponent } from '../../../util';

/** 视频播放属性 */
export interface VideoPlayerProperty {
  videoUrl: string; // 视频链接
  posterUrl: string; // 封面链接
  autoplay: boolean; // 是否自动播放
  style: VideoPlayerStyle; // 组件样式
}

/** 视频播放样式 */
export interface VideoPlayerStyle extends ComponentStyle {
  height: number; // 视频高度
}

/** 定义组件 */
export const component = {
  id: 'VideoPlayer',
  name: '视频播放',
  icon: 'lucide:video',
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
