import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AiKnowledgeSegmentApi {
  // AI 知识库分段
  export interface KnowledgeSegment {
    id: number; // 编号
    documentId: number; // 文档编号
    knowledgeId: number; // 知识库编号
    vectorId: string; // 向量库编号
    content: string; // 切片内容
    contentLength: number; // 切片内容长度
    tokens: number; // token 数量
    retrievalCount: number; // 召回次数
    status: number; // 文档状态
    createTime: number; // 创建时间
  }
}

// 查询知识库分段分页
export function getKnowledgeSegmentPage(params: PageParam) {
  return requestClient.get<PageResult<AiKnowledgeSegmentApi.KnowledgeSegment>>(
    '/ai/knowledge/segment/page',
    { params },
  );
}

// 查询知识库分段详情
export function getKnowledgeSegment(id: number) {
  return requestClient.get<AiKnowledgeSegmentApi.KnowledgeSegment>(
    `/ai/knowledge/segment/get?id=${id}`,
  );
}
// 新增知识库分段
export function createKnowledgeSegment(
  data: AiKnowledgeSegmentApi.KnowledgeSegment,
) {
  return requestClient.post('/ai/knowledge/segment/create', data);
}

// 修改知识库分段
export function updateKnowledgeSegment(
  data: AiKnowledgeSegmentApi.KnowledgeSegment,
) {
  return requestClient.put('/ai/knowledge/segment/update', data);
}

// 修改知识库分段状态
export function updateKnowledgeSegmentStatus(data: any) {
  return requestClient.put('/ai/knowledge/segment/update-status', data);
}
// 删除知识库分段
export function deleteKnowledgeSegment(id: number) {
  return requestClient.delete(`/ai/knowledge/segment/delete?id=${id}`);
}

// 切片内容
export function splitContent(url: string, segmentMaxTokens: number) {
  return requestClient.get('/ai/knowledge/segment/split', {
    params: { url, segmentMaxTokens },
  });
}

// 获取文档处理列表
export function getKnowledgeSegmentProcessList(documentIds: number[]) {
  return requestClient.get('/ai/knowledge/segment/get-process-list', {
    params: { documentIds: documentIds.join(',') },
  });
}

// 搜索知识库分段
export function searchKnowledgeSegment(params: any) {
  return requestClient.get('/ai/knowledge/segment/search', {
    params,
  });
}
