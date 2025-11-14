import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

/** 公告栏属性 */
export interface NoticeBarProperty {
  // 图标地址
  iconUrl: string;
  // 公告内容列表
  contents: NoticeContentProperty[];
  // 背景颜色
  backgroundColor: string;
  // 文字颜色
  textColor: string;
  // 组件样式
  style: ComponentStyle;
}

/** 内容属性 */
export interface NoticeContentProperty {
  // 内容文字
  text: string;
  // 链接地址
  url: string;
}

// 定义组件
export const component = {
  id: 'NoticeBar',
  name: '公告栏',
  icon: 'ep:bell',
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
