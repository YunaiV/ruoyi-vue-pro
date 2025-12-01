import { requestClient } from '#/api/request';

export namespace MallKefuConversationApi {
  /** 客服会话 */
  export interface Conversation {
    id: number; // 编号
    userId: number; // 会话所属用户
    userAvatar: string; // 会话所属用户头像
    userNickname: string; // 会话所属用户昵称
    lastMessageTime: Date; // 最后聊天时间
    lastMessageContent: string; // 最后聊天内容
    lastMessageContentType: number; // 最后发送的消息类型
    adminPinned: boolean; // 管理端置顶
    userDeleted: boolean; // 用户是否可见
    adminDeleted: boolean; // 管理员是否可见
    adminUnreadMessageCount: number; // 管理员未读消息数
    createTime?: string; // 创建时间
  }

  /** 会话置顶请求 */
  export interface ConversationPinnedUpdateReqVO {
    id: number; // 会话编号
    pinned: boolean; // 是否置顶
  }
}

/** 获得客服会话列表 */
export function getConversationList() {
  return requestClient.get<MallKefuConversationApi.Conversation[]>(
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
  data: MallKefuConversationApi.ConversationPinnedUpdateReqVO,
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
