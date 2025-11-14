import type { PageResult } from '@vben/request';

import { useAppConfig } from '@vben/hooks';
import { fetchEventSource } from '@vben/request';
import { useAccessStore } from '@vben/stores';

import { requestClient } from '#/api/request';

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);
const accessStore = useAccessStore();
export namespace AiChatMessageApi {
  export interface ChatMessage {
    id: number; // 编号
    conversationId: number; // 对话编号
    type: string; // 消息类型
    userId: string; // 用户编号
    roleId: string; // 角色编号
    model: number; // 模型标志
    modelId: number; // 模型编号
    content: string; // 聊天内容
    tokens: number; // 消耗 Token 数量
    segmentIds?: number[]; // 段落编号
    segments?: {
      content: string; // 段落内容
      documentId: number; // 文档编号
      documentName: string; // 文档名称
      id: number; // 段落编号
    }[];
    createTime: Date; // 创建时间
    roleAvatar: string; // 角色头像
    userAvatar: string; // 用户头像
  }
}

// 消息列表
export function getChatMessageListByConversationId(
  conversationId: null | number,
) {
  return requestClient.get<AiChatMessageApi.ChatMessage[]>(
    `/ai/chat/message/list-by-conversation-id?conversationId=${conversationId}`,
  );
}

// 发送 Stream 消息
export function sendChatMessageStream(
  conversationId: number,
  content: string,
  ctrl: any,
  enableContext: boolean,
  onMessage: any,
  onError: any,
  onClose: any,
) {
  const token = accessStore.accessToken;
  return fetchEventSource(`${apiURL}/ai/chat/message/send-stream`, {
    method: 'post',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    openWhenHidden: true,
    body: JSON.stringify({
      conversationId,
      content,
      useContext: enableContext,
    }),
    onmessage: onMessage,
    onerror: onError,
    onclose: onClose,
    signal: ctrl.signal,
  });
}

// 删除消息
export function deleteChatMessage(id: number) {
  return requestClient.delete(`/ai/chat/message/delete?id=${id}`);
}

// 删除指定对话的消息
export function deleteByConversationId(conversationId: number) {
  return requestClient.delete(
    `/ai/chat/message/delete-by-conversation-id?conversationId=${conversationId}`,
  );
}
// 获得消息分页
export function getChatMessagePage(params: any) {
  return requestClient.get<PageResult<AiChatMessageApi.ChatMessage>>(
    '/ai/chat/message/page',
    { params },
  );
}
// 管理员删除消息
export function deleteChatMessageByAdmin(id: number) {
  return requestClient.delete(`/ai/chat/message/delete-by-admin?id=${id}`);
}
