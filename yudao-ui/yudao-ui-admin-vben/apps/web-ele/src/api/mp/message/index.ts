import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

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

export namespace MpMessageApi {
  /** 消息信息 */
  export interface Message {
    id?: number;
    accountId: number;
    type: MessageType;
    openid: string;
    content: string;
    mediaId?: string;
    status: number;
    remark?: string;
    createTime?: Date;
  }

  /** 发送消息请求 */
  export interface SendMessageRequest {
    accountId: number;
    openid: string;
    type: MessageType;
    content: string;
    mediaId?: string;
  }
}

/** 查询消息列表 */
export function getMessagePage(params: PageParam) {
  return requestClient.get<PageResult<MpMessageApi.Message>>(
    '/mp/message/page',
    {
      params,
    },
  );
}

/** 发送消息 */
export function sendMessage(data: MpMessageApi.SendMessageRequest) {
  return requestClient.post('/mp/message/send', data);
}
