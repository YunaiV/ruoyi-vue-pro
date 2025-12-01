import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallKefuMessageApi {
  /** 客服消息 */
  export interface Message {
    id: number; // 编号
    conversationId: number; // 会话编号
    senderId: number; // 发送人编号
    senderAvatar: string; // 发送人头像
    senderType: number; // 发送人类型
    receiverId: number; // 接收人编号
    receiverType: number; // 接收人类型
    contentType: number; // 消息类型
    content: string; // 消息内容
    readStatus: boolean; // 是否已读
    createTime: Date; // 创建时间
  }

  /** 发送消息请求 */
  export interface MessageSend {
    conversationId: number; // 会话编号
    contentType: number; // 消息类型
    content: string; // 消息内容
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
export function getKeFuMessageList(params: PageParam) {
  return requestClient.get<PageResult<MallKefuMessageApi.Message>>(
    '/promotion/kefu-message/list',
    { params },
  );
}
