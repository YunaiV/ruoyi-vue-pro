import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace BpmProcessListenerApi {
  /** BPM 流程监听器 */
  export interface ProcessListener {
    id: number; // 编号
    name: string; // 监听器名字
    type: string; // 监听器类型
    status: number; // 监听器状态
    event: string; // 监听事件
    valueType: string; // 监听器值类型
    value: string; // 监听器值
  }
}

/** 查询流程监听器分页 */
export async function getProcessListenerPage(params: PageParam) {
  return requestClient.get<PageResult<BpmProcessListenerApi.ProcessListener>>(
    '/bpm/process-listener/page',
    { params },
  );
}

/** 查询流程监听器详情 */
export async function getProcessListener(id: number) {
  return requestClient.get<BpmProcessListenerApi.ProcessListener>(
    `/bpm/process-listener/get?id=${id}`,
  );
}

/** 新增流程监听器 */
export async function createProcessListener(
  data: BpmProcessListenerApi.ProcessListener,
) {
  return requestClient.post<number>('/bpm/process-listener/create', data);
}

/** 修改流程监听器 */
export async function updateProcessListener(
  data: BpmProcessListenerApi.ProcessListener,
) {
  return requestClient.put<boolean>('/bpm/process-listener/update', data);
}

/** 删除流程监听器 */
export async function deleteProcessListener(id: number) {
  return requestClient.delete<boolean>(`/bpm/process-listener/delete?id=${id}`);
}
