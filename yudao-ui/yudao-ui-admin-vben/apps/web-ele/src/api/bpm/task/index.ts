import type { PageParam, PageResult } from '@vben/request';

import type { BpmProcessInstanceApi } from '../processInstance';

import { requestClient } from '#/api/request';

export namespace BpmTaskApi {
  /** BPM 流程监听器 */
  export interface Task {
    id: number; // 编号
    name: string; // 监听器名字
    type: string; // 监听器类型
    status: number; // 监听器状态
    event: string; // 监听事件
    valueType: string; // 监听器值类型
  }

  // 流程任务
  export interface TaskManager {
    id: string; // 编号
    name: string; // 任务名称
    createTime: number; // 创建时间
    endTime: number; // 结束时间
    durationInMillis: number; // 持续时间
    status: number; // 状态
    reason: string; // 原因
    ownerUser: any; // 负责人
    assigneeUser: any; // 处理人
    taskDefinitionKey: string; // 任务定义key
    processInstanceId: string; // 流程实例id
    processInstance: BpmProcessInstanceApi.ProcessInstance; // 流程实例
    parentTaskId: any; // 父任务id
    children: any; // 子任务
    formId: any; // 表单id
    formName: any; // 表单名称
    formConf: any; // 表单配置
    formFields: any; // 表单字段
    formVariables: any; // 表单变量
    buttonsSetting: any; // 按钮设置
    signEnable: any; // 签名设置
    reasonRequire: any; // 原因设置
    nodeType: any; // 节点类型
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

/** 退回 */
export const returnTask = async (data: any) => {
  return await requestClient.put('/bpm/task/return', data);
};

// 委派
export const delegateTask = async (data: any) => {
  return await requestClient.put('/bpm/task/delegate', data);
};

// 转派
export const transferTask = async (data: any) => {
  return await requestClient.put('/bpm/task/transfer', data);
};

// 加签
export const signCreateTask = async (data: any) => {
  return await requestClient.put('/bpm/task/create-sign', data);
};

// 减签
export const signDeleteTask = async (data: any) => {
  return await requestClient.delete('/bpm/task/delete-sign', data);
};

// 抄送
export const copyTask = async (data: any) => {
  return await requestClient.put('/bpm/task/copy', data);
};

// 获取我的待办任务
export const myTodoTask = async (processInstanceId: string) => {
  return await requestClient.get(
    `/bpm/task/my-todo?processInstanceId=${processInstanceId}`,
  );
};

// 获取加签任务列表
export const getChildrenTaskList = async (id: string) => {
  return await requestClient.get(
    `/bpm/task/list-by-parent-task-id?parentTaskId=${id}`,
  );
};
