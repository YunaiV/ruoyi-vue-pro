import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AiModelToolApi {
  export interface Tool {
    id: number; // 工具编号
    name: string; // 工具名称
    description: string; // 工具描述
    status: number; // 状态
  }
}

// 查询工具分页
export function getToolPage(params: PageParam) {
  return requestClient.get<PageResult<AiModelToolApi.Tool>>('/ai/tool/page', {
    params,
  });
}

// 查询工具详情
export function getTool(id: number) {
  return requestClient.get<AiModelToolApi.Tool>(`/ai/tool/get?id=${id}`);
}
// 新增工具
export function createTool(data: AiModelToolApi.Tool) {
  return requestClient.post('/ai/tool/create', data);
}

// 修改工具
export function updateTool(data: AiModelToolApi.Tool) {
  return requestClient.put('/ai/tool/update', data);
}

// 删除工具
export function deleteTool(id: number) {
  return requestClient.delete(`/ai/tool/delete?id=${id}`);
}

// 获取工具简单列表
export function getToolSimpleList() {
  return requestClient.get<AiModelToolApi.Tool[]>('/ai/tool/simple-list');
}
