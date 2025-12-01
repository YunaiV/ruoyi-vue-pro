import type { PageParam, PageResult } from '@vben/request';

import type { BpmProcessInstanceApi } from '../processInstance';

import { requestClient } from '#/api/request';

export namespace BpmTaskApi {
  /** 流程任务 */
  export interface Task {
    id: number; // 编号
    name: string; // 监听器名字
    type: string; // 监听器类型
    status: number; // 监听器状态
    event: string; // 监听事件
    valueType: string; // 监听器值类型
    processInstance?: BpmProcessInstanceApi.ProcessInstance; // 流程实例
  }
}

/** 查询待办任务分页 */
export async function getTaskTodoPage(params: PageParam) {
  return requestClient.get<PageResult<BpmTaskApi.Task>>('/bpm/task/todo-page', {
    params,
  });
}

/** 查询已办任务分页 */
export async function getTaskDonePage(params: PageParam) {
  return requestClient.get<PageResult<BpmTaskApi.Task>>('/bpm/task/done-page', {
    params,
  });
}

/** 查询任务管理分页 */
export async function getTaskManagerPage(params: PageParam) {
  return requestClient.get<PageResult<BpmTaskApi.Task>>(
    '/bpm/task/manager-page',
    { params },
  );
}

/** 审批任务 */
export const approveTask = async (data: any) => {
  return await requestClient.put('/bpm/task/approve', data);
};

/** 驳回任务 */
export const rejectTask = async (data: any) => {
  return await requestClient.put('/bpm/task/reject', data);
};

/** 根据流程实例 ID 查询任务列表 */
export const getTaskListByProcessInstanceId = async (id: string) => {
  return await requestClient.get(
    `/bpm/task/list-by-process-instance-id?processInstanceId=${id}`,
  );
};

/** 获取所有可退回的节点 */
export const getTaskListByReturn = async (id: string) => {
  return await requestClient.get(`/bpm/task/list-by-return?id=${id}`);
};

/** 退回任务 */
export const returnTask = async (data: any) => {
  return await requestClient.put('/bpm/task/return', data);
};

/** 委派任务 */
export const delegateTask = async (data: any) => {
  return await requestClient.put('/bpm/task/delegate', data);
};

/** 转派任务 */
export const transferTask = async (data: any) => {
  return await requestClient.put('/bpm/task/transfer', data);
};

/** 加签任务 */
export const signCreateTask = async (data: any) => {
  return await requestClient.put('/bpm/task/create-sign', data);
};

/** 减签任务 */
export const signDeleteTask = async (data: any) => {
  return await requestClient.delete('/bpm/task/delete-sign', data);
};

/** 抄送任务 */
export const copyTask = async (data: any) => {
  return await requestClient.put('/bpm/task/copy', data);
};

/** 获取加签任务列表 */
export const getChildrenTaskList = async (id: string) => {
  return await requestClient.get(
    `/bpm/task/list-by-parent-task-id?parentTaskId=${id}`,
  );
};

/** 撤回任务 */
export const withdrawTask = async (taskId: string) => {
  return await requestClient.put('/bpm/task/withdraw', null, {
    params: { taskId },
  });
};
