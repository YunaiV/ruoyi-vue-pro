import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AiKnowledgeDocumentApi {
  export interface KnowledgeDocument {
    id: number; // 编号
    knowledgeId: number; // 知识库编号
    name: string; // 文档名称
    contentLength: number; // 字符数
    tokens: number; // token 数
    segmentMaxTokens: number; // 分片最大 token 数
    retrievalCount: number; // 召回次数
    status: number; // 是否启用
  }
}

// 查询知识库文档分页
export function getKnowledgeDocumentPage(params: PageParam) {
  return requestClient.get<
    PageResult<AiKnowledgeDocumentApi.KnowledgeDocument>
  >('/ai/knowledge/document/page', { params });
}

// 查询知识库文档详情
export function getKnowledgeDocument(id: number) {
  return requestClient.get(`/ai/knowledge/document/get?id=${id}`);
}

// 新增知识库文档（单个）
export function createKnowledge(data: any) {
  return requestClient.post('/ai/knowledge/document/create', data);
}

// 新增知识库文档（多个）
export function createKnowledgeDocumentList(data: any) {
  return requestClient.post('/ai/knowledge/document/create-list', data);
}

// 修改知识库文档
export function updateKnowledgeDocument(data: any) {
  return requestClient.put('/ai/knowledge/document/update', data);
}

// 修改知识库文档状态
export function updateKnowledgeDocumentStatus(data: any) {
  return requestClient.put('/ai/knowledge/document/update-status', data);
}

// 删除知识库文档
export function deleteKnowledgeDocument(id: number) {
  return requestClient.delete(`/ai/knowledge/document/delete?id=${id}`);
}
