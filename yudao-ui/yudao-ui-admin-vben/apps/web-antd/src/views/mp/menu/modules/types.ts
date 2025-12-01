export interface Replay {
  title: string;
  description: string;
  picUrl: string;
  url: string;
}

export type MenuType =
  | ''
  | 'article_view_limited'
  | 'click'
  | 'location_select'
  | 'pic_photo_or_album'
  | 'pic_sysphoto'
  | 'pic_weixin'
  | 'scancode_push'
  | 'scancode_waitmsg'
  | 'view';

interface _RawMenu {
  // db
  id: number;
  parentId: number;
  accountId: number;
  appId: string;
  createTime: number;

  // mp-native
  name: string;
  menuKey: string;
  type: MenuType;
  url: string;
  miniProgramAppId: string;
  miniProgramPagePath: string;
  articleId: string;
  replyMessageType: string;
  replyContent: string;
  replyMediaId: string;
  replyMediaUrl: string;
  replyThumbMediaId: string;
  replyThumbMediaUrl: string;
  replyTitle: string;
  replyDescription: string;
  replyArticles: Replay;
  replyMusicUrl: string;
  replyHqMusicUrl: string;
}

export type RawMenu = Partial<_RawMenu>;

interface _Reply {
  type: string;
  accountId: number;
  content: string;
  mediaId: string;
  url: string;
  thumbMediaId: string;
  thumbMediaUrl: string;
  title: string;
  description: string;
  articles: null | Replay[];
  musicUrl: string;
  hqMusicUrl: string;
}

export type Reply = Partial<_Reply>;

interface _Menu extends RawMenu {
  children: _Menu[];
  reply: Reply;
}

export type Menu = Partial<_Menu>;

export const menuOptions = [
  {
    value: 'view',
    label: '跳转网页',
  },
  {
    value: 'miniprogram',
    label: '跳转小程序',
  },
  {
    value: 'click',
    label: '点击回复',
  },
  {
    value: 'article_view_limited',
    label: '跳转图文消息',
  },
  {
    value: 'scancode_push',
    label: '扫码直接返回结果',
  },
  {
    value: 'scancode_waitmsg',
    label: '扫码回复',
  },
  {
    value: 'pic_sysphoto',
    label: '系统拍照发图',
  },
  {
    value: 'pic_photo_or_album',
    label: '拍照或者相册',
  },
  {
    value: 'pic_weixin',
    label: '微信相册',
  },
  {
    value: 'location_select',
    label: '选择地理位置',
  },
];
