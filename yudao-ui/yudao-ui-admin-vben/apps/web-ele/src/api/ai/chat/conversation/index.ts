import type { PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AiChatConversationApi {
  export interface ChatConversation {
    id: number; // ID 编号
    userId: number; // 用户编号
    title: string; // 对话标题
    pinned: boolean; // 是否置顶
    roleId: number; // 角色编号
    modelId: number; // 模型编号
    model: string; // 模型标志
    temperature: number; // 温度参数
    maxTokens: number; // 单条回复的最大 Token 数量
    maxContexts: number; // 上下文的最大 Message 数量
    createTime?: Date; // 创建时间
    // 额外字段
    systemMessage?: string; // 角色设定
    modelName?: string; // 模型名字
    roleAvatar?: string; // 角色头像
    modelMaxTokens?: string; // 模型的单条回复的最大 Token 数量
    modelMaxContexts?: string; // 模型的上下文的最大 Message 数量
  }
}

// 获得【我的】聊天对话
export function getChatConversationMy(id: number) {
  return requestClient.get<AiChatConversationApi.ChatConversation>(
    `/ai/chat/conversation/get-my?id=${id}`,
  );
}

// 新增【我的】聊天对话
export function createChatConversationMy(
  data: AiChatConversationApi.ChatConversation,
) {
  return requestClient.post('/ai/chat/conversation/create-my', data);
}

//  更新【我的】聊天对话
export function updateChatConversationMy(
  data: AiChatConversationApi.ChatConversation,
) {
  return requestClient.put(`/ai/chat/conversation/update-my`, data);
}

//  删除【我的】聊天对话
export function deleteChatConversationMy(id: number) {
  return requestClient.delete(`/ai/chat/conversation/delete-my?id=${id}`);
}

//  删除【我的】所有对话，置顶除外
export function deleteChatConversationMyByUnpinned() {
  return requestClient.delete(`/ai/chat/conversation/delete-by-unpinned`);
}

//  获得【我的】聊天对话列表
export function getChatConversationMyList() {
  return requestClient.get<AiChatConversationApi.ChatConversation[]>(
    `/ai/chat/conversation/my-list`,
  );
}

//  获得【我的】聊天对话列表
export function getChatConversationPage(params: any) {
  return requestClient.get<
    PageResult<AiChatConversationApi.ChatConversation[]>
  >(`/ai/chat/conversation/page`, { params });
}

//  管理员删除消息
export function deleteChatConversationByAdmin(id: number) {
  return requestClient.delete(`/ai/chat/conversation/delete-by-admin?id=${id}`);
}
