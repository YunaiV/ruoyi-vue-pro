import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AiModelChatRoleApi {
  export interface ChatRole {
    id: number; // 角色编号
    modelId: number; // 模型编号
    name: string; // 角色名称
    avatar: string; // 角色头像
    category: string; // 角色类别
    sort: number; // 角色排序
    description: string; // 角色描述
    systemMessage: string; // 角色设定
    welcomeMessage: string; // 角色设定
    publicStatus: boolean; // 是否公开
    status: number; // 状态
    knowledgeIds?: number[]; // 引用的知识库 ID 列表
    toolIds?: number[]; // 引用的工具 ID 列表
  }

  // AI 聊天角色 分页请求
  export interface ChatRolePageReq {
    name?: string; // 角色名称
    category?: string; // 角色类别
    publicStatus: boolean; // 是否公开
    pageNo: number; // 是否公开
    pageSize: number; // 是否公开
  }
}

// 查询聊天角色分页
export function getChatRolePage(params: PageParam) {
  return requestClient.get<PageResult<AiModelChatRoleApi.ChatRole>>(
    '/ai/chat-role/page',
    { params },
  );
}

// 查询聊天角色详情
export function getChatRole(id: number) {
  return requestClient.get<AiModelChatRoleApi.ChatRole>(
    `/ai/chat-role/get?id=${id}`,
  );
}
// 新增聊天角色
export function createChatRole(data: AiModelChatRoleApi.ChatRole) {
  return requestClient.post('/ai/chat-role/create', data);
}

// 修改聊天角色
export function updateChatRole(data: AiModelChatRoleApi.ChatRole) {
  return requestClient.put('/ai/chat-role/update', data);
}

// 删除聊天角色
export function deleteChatRole(id: number) {
  return requestClient.delete(`/ai/chat-role/delete?id=${id}`);
}

// ======= chat 聊天
// 获取 my role
export function getMyPage(params: AiModelChatRoleApi.ChatRolePageReq) {
  return requestClient.get('/ai/chat-role/my-page', { params });
}

// 获取角色分类
export function getCategoryList() {
  return requestClient.get('/ai/chat-role/category-list');
}

// 创建角色
export function createMy(data: AiModelChatRoleApi.ChatRole) {
  return requestClient.post('/ai/chat-role/create-my', data);
}

// 更新角色
export function updateMy(data: AiModelChatRoleApi.ChatRole) {
  return requestClient.put('/ai/chat-role/update', data);
}

// 删除角色 my
export function deleteMy(id: number) {
  return requestClient.delete(`/ai/chat-role/delete-my?id=${id}`);
}
