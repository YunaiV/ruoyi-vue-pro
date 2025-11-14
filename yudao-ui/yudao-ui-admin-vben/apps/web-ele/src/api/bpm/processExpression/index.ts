import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace BpmProcessExpressionApi {
  /** 流程表达式 */
  export interface ProcessExpression {
    id: number; // 编号
    name: string; // 表达式名字
    status: number; // 表达式状态
    expression: string; // 表达式
  }
}

/** 查询流程表达式分页 */
export async function getProcessExpressionPage(params: PageParam) {
  return requestClient.get<
    PageResult<BpmProcessExpressionApi.ProcessExpression>
  >('/bpm/process-expression/page', { params });
}

/** 查询流程表达式详情 */
export async function getProcessExpression(id: number) {
  return requestClient.get<BpmProcessExpressionApi.ProcessExpression>(
    `/bpm/process-expression/get?id=${id}`,
  );
}

/** 新增流程表达式 */
export async function createProcessExpression(
  data: BpmProcessExpressionApi.ProcessExpression,
) {
  return requestClient.post<number>('/bpm/process-expression/create', data);
}

/** 修改流程表达式 */
export async function updateProcessExpression(
  data: BpmProcessExpressionApi.ProcessExpression,
) {
  return requestClient.put<boolean>('/bpm/process-expression/update', data);
}

/** 删除流程表达式 */
export async function deleteProcessExpression(id: number) {
  return requestClient.delete<boolean>(
    `/bpm/process-expression/delete?id=${id}`,
  );
}

/** 导出流程表达式 */
export async function exportProcessExpression(params: any) {
  return requestClient.download('/bpm/process-expression/export-excel', {
    params,
  });
}
