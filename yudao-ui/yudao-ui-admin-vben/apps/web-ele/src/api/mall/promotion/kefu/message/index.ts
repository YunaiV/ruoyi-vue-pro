import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallKefuMessageApi {
  /** 客服消息 */
  export interface Message {
    /** 编号 */
    id: number;
    /** 会话编号 */
    conversationId: number;
    /** 发送人编号 */
    senderId: number;
    /** 发送人头像 */
    senderAvatar: string;
    /** 发送人类型 */
    senderType: number;
    /** 接收人编号 */
    receiverId: number;
    /** 接收人类型 */
    receiverType: number;
    /** 消息类型 */
    contentType: number;
    /** 消息内容 */
    content: string;
    /** 是否已读 */
    readStatus: boolean;
    /** 创建时间 */
    createTime: Date;
  }

  /** 发送消息请求 */
  export interface MessageSend {
    /** 会话编号 */
    conversationId: number;
    /** 消息类型 */
    contentType: number;
    /** 消息内容 */
    content: string;
  }

  /** 消息列表查询参数 */
  export interface MessageQuery extends PageParam {
    /** 会话编号 */
    conversationId: number;
  }
}

/** 发送客服消息 */
export function sendKeFuMessage(data: MallKefuMessageApi.MessageSend) {
  return requestClient.post('/promotion/kefu-message/send', data);
}

/** 更新客服消息已读状态 */
export function updateKeFuMessageReadStatus(conversationId: number) {
  return requestClient.put(
    `/promotion/kefu-message/update-read-status?conversationId=${conversationId}`,
  );
}

/** 获得消息列表（流式加载） */
export function getKeFuMessageList(params: MallKefuMessageApi.MessageQuery) {
  return requestClient.get<PageResult<MallKefuMessageApi.Message>>(
    '/promotion/kefu-message/list',
    { params },
  );
}
