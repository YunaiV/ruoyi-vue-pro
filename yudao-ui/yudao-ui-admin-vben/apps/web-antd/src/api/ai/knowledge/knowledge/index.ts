import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AiKnowledgeKnowledgeApi {
  export interface Knowledge {
    id: number; // 编号
    name: string; // 知识库名称
    description: string; // 知识库描述
    embeddingModelId: number; // 嵌入模型编号，高质量模式时维护
    topK: number; // topK
    similarityThreshold: number; // 相似度阈值
  }
}

// 查询知识库分页
export function getKnowledgePage(params: PageParam) {
  return requestClient.get<PageResult<AiKnowledgeKnowledgeApi.Knowledge>>(
    '/ai/knowledge/page',
    { params },
  );
}

// 查询知识库详情
export function getKnowledge(id: number) {
  return requestClient.get<AiKnowledgeKnowledgeApi.Knowledge>(
    `/ai/knowledge/get?id=${id}`,
  );
}
// 新增知识库
export function createKnowledge(data: AiKnowledgeKnowledgeApi.Knowledge) {
  return requestClient.post('/ai/knowledge/create', data);
}

// 修改知识库
export function updateKnowledge(data: AiKnowledgeKnowledgeApi.Knowledge) {
  return requestClient.put('/ai/knowledge/update', data);
}

// 删除知识库
export function deleteKnowledge(id: number) {
  return requestClient.delete(`/ai/knowledge/delete?id=${id}`);
}

// 获取知识库简单列表
export function getSimpleKnowledgeList() {
  return requestClient.get<AiKnowledgeKnowledgeApi.Knowledge[]>(
    '/ai/knowledge/simple-list',
  );
}
