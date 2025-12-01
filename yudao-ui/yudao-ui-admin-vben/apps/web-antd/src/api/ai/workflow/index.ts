import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AiWorkflowApi {
  /** 工作流 */
  export interface Workflow {
    id?: number; // 编号
    name: string; // 工作流名称
    code: string; // 工作流标识
    graph: string; // 工作流模型 JSON 数据
    remark?: string; // 备注
    status: number; // 状态
    createTime?: Date; // 创建时间
  }
}

/** 查询工作流管理列表 */
export function getWorkflowPage(params: PageParam) {
  return requestClient.get<PageResult<AiWorkflowApi.Workflow>>(
    '/ai/workflow/page',
    { params },
  );
}

/** 查询工作流详情 */
export function getWorkflow(id: number) {
  return requestClient.get<AiWorkflowApi.Workflow>(`/ai/workflow/get?id=${id}`);
}

/** 新增工作流 */
export function createWorkflow(data: AiWorkflowApi.Workflow) {
  return requestClient.post('/ai/workflow/create', data);
}

/** 修改工作流 */
export function updateWorkflow(data: AiWorkflowApi.Workflow) {
  return requestClient.put('/ai/workflow/update', data);
}

/** 删除工作流 */
export function deleteWorkflow(id: number) {
  return requestClient.delete(`/ai/workflow/delete?id=${id}`);
}

/** 测试工作流 */
export function testWorkflow(data: any) {
  return requestClient.post('/ai/workflow/test', data);
}
