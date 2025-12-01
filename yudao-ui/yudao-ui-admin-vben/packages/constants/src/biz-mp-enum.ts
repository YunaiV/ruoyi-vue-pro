/** 回复类型枚举 */
export enum ReplyType {
  Image = 'image',
  Music = 'music',
  News = 'news',
  Text = 'text',
  Video = 'video',
  Voice = 'voice',
}

/** 图文类型枚举 */
export enum NewsType {
  Draft = '2',
  Published = '1',
}

/** 回复素材类型枚举 */
export enum ReplyMaterialType {
  Image = 'image',
  News = 'news',
  Video = 'video',
  Voice = 'voice',
}

/** 微信消息类型枚举 */
export enum MpMsgType {
  Event = 'event',
  Image = 'image',
  Link = 'link',
  Location = 'location',
  Music = 'music',
  News = 'news',
  ShortVideo = 'shortvideo',
  Text = 'text',
  Video = 'video',
  Voice = 'voice',
}

/** 自动回复消息类型枚举（Follow: 关注时回复；Message: 消息回复；Keyword: 关键词回复） */
export enum AutoReplyMsgType {
  Follow = 1,
  Keyword = 3,
  Message = 2,
}

/** 消息类型枚举 */
export enum MessageType {
  IMAGE = 'image', // 图片消息
  MPNEWS = 'mpnews', // 公众号图文消息
  MUSIC = 'music', // 音乐消息
  NEWS = 'news', // 图文消息
  TEXT = 'text', // 文本消息
  VIDEO = 'video', // 视频消息
  VOICE = 'voice', // 语音消息
  WXCARD = 'wxcard', // 卡券消息
}

/** 素材类型枚举 */
export enum MaterialType {
  IMAGE = 1, // 图片
  THUMB = 4, // 缩略图
  VIDEO = 3, // 视频
  VOICE = 2, // 语音
}

/** 菜单类型枚举 */
export enum MenuType {
  CLICK = 'click', // 点击推事件
  LOCATION_SELECT = 'location_select', // 发送位置
  MEDIA_ID = 'media_id', // 下发消息
  MINIPROGRAM = 'miniprogram', // 小程序
  PIC_PHOTO_OR_ALBUM = 'pic_photo_or_album', // 拍照或者相册发图
  PIC_SYSPHOTO = 'pic_sysphoto', // 系统拍照发图
  PIC_WEIXIN = 'pic_weixin', // 微信相册发图
  SCANCODE_PUSH = 'scancode_push', // 扫码推事件
  SCANCODE_WAITMSG = 'scancode_waitmsg', // 扫码带提示
  VIEW = 'view', // 跳转 URL
  VIEW_LIMITED = 'view_limited', // 跳转图文消息URL
}

/** 允许选择的请求消息类型 */
export const RequestMessageTypes = new Set([
  'image',
  'link',
  'location',
  'shortvideo',
  'text',
  'video',
  'voice',
]);
