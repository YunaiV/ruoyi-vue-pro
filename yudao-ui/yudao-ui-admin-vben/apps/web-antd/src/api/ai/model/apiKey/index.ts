import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AiModelApiKeyApi {
  export interface ApiKey {
    id: number; // 编号
    name: string; // 名称
    apiKey: string; // 密钥
    platform: string; // 平台
    url: string; // 自定义 API 地址
    status: number; // 状态
  }
}

// 查询 API 密钥分页
export function getApiKeyPage(params: PageParam) {
  return requestClient.get<PageResult<AiModelApiKeyApi.ApiKey>>(
    '/ai/api-key/page',
    { params },
  );
}

// 获得 API 密钥列表
export function getApiKeySimpleList() {
  return requestClient.get<AiModelApiKeyApi.ApiKey[]>(
    '/ai/api-key/simple-list',
  );
}

// 查询 API 密钥详情
export function getApiKey(id: number) {
  return requestClient.get<AiModelApiKeyApi.ApiKey>(`/ai/api-key/get?id=${id}`);
}
// 新增 API 密钥
export function createApiKey(data: AiModelApiKeyApi.ApiKey) {
  return requestClient.post('/ai/api-key/create', data);
}

// 修改 API 密钥
export function updateApiKey(data: AiModelApiKeyApi.ApiKey) {
  return requestClient.put('/ai/api-key/update', data);
}

// 删除 API 密钥
export function deleteApiKey(id: number) {
  return requestClient.delete(`/ai/api-key/delete?id=${id}`);
}
