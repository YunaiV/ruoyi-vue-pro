import type { PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallKefuConversationApi {
  /** 客服会话 */
  export interface Conversation {
    /** 编号 */
    id: number;
    /** 会话所属用户 */
    userId: number;
    /** 会话所属用户头像 */
    userAvatar: string;
    /** 会话所属用户昵称 */
    userNickname: string;
    /** 最后聊天时间 */
    lastMessageTime: Date;
    /** 最后聊天内容 */
    lastMessageContent: string;
    /** 最后发送的消息类型 */
    lastMessageContentType: number;
    /** 管理端置顶 */
    adminPinned: boolean;
    /** 用户是否可见 */
    userDeleted: boolean;
    /** 管理员是否可见 */
    adminDeleted: boolean;
    /** 管理员未读消息数 */
    adminUnreadMessageCount: number;
    /** 创建时间 */
    createTime?: string;
  }

  /** 会话置顶请求 */
  export interface ConversationPinnedUpdate {
    /** 会话编号 */
    id: number;
    /** 是否置顶 */
    pinned: boolean;
  }
}

/** 获得客服会话列表 */
export function getConversationList() {
  return requestClient.get<PageResult<MallKefuConversationApi.Conversation>>(
    '/promotion/kefu-conversation/list',
  );
}

/** 获得客服会话 */
export function getConversation(id: number) {
  return requestClient.get<MallKefuConversationApi.Conversation>(
    `/promotion/kefu-conversation/get?id=${id}`,
  );
}

/** 客服会话置顶 */
export function updateConversationPinned(
  data: MallKefuConversationApi.ConversationPinnedUpdate,
) {
  return requestClient.put(
    '/promotion/kefu-conversation/update-conversation-pinned',
    data,
  );
}

/** 删除客服会话 */
export function deleteConversation(id: number) {
  return requestClient.delete(`/promotion/kefu-conversation/delete?id=${id}`);
}
