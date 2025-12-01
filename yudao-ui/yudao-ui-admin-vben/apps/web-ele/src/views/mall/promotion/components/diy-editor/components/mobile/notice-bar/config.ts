import type { ComponentStyle, DiyComponent } from '../../../util';

/** 公告栏属性 */
export interface NoticeBarProperty {
  iconUrl: string; // 图标地址
  contents: NoticeContentProperty[]; // 公告内容列表
  backgroundColor: string; // 背景颜色
  textColor: string; // 文字颜色
  style: ComponentStyle; // 组件样式
}

/** 内容属性 */
export interface NoticeContentProperty {
  text: string; // 内容文字
  url: string; // 链接地址
}

/** 定义组件 */
export const component = {
  id: 'NoticeBar',
  name: '公告栏',
  icon: 'lucide:bell',
  property: {
    iconUrl: 'http://mall.yudao.iocoder.cn/static/images/xinjian.png',
    contents: [
      {
        text: '',
        url: '',
      },
    ],
    backgroundColor: '#fff',
    textColor: '#333',
    style: {
      bgType: 'color',
      bgColor: '#fff',
      marginBottom: 8,
    } as ComponentStyle,
  },
} as DiyComponent<NoticeBarProperty>;
