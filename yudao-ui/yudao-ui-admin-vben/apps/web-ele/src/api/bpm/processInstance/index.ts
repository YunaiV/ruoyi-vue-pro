import type { PageParam, PageResult } from '@vben/request';
import type { BpmCandidateStrategyEnum, BpmNodeTypeEnum } from '@vben/utils';

import type { BpmTaskApi } from '../task';

import type { BpmModelApi } from '#/api/bpm/model';

import { requestClient } from '#/api/request';

export namespace BpmProcessInstanceApi {
  // TODO @芋艿：一些注释缺少或者不对；
  export interface Task {
    id: number;
    name: string;
  }

  export interface User {
    avatar: string;
    id: number;
    nickname: string;
  }

  // 审批任务信息
  export interface ApprovalTaskInfo {
    assigneeUser: User;
    id: number;
    ownerUser: User;
    reason: string;
    signPicUrl: string;
    status: number;
  }

  // 审批节点信息
  export interface ApprovalNodeInfo {
    candidateStrategy?: BpmCandidateStrategyEnum;
    candidateUsers?: User[];
    endTime?: Date;
    id: number;
    name: string;
    nodeType: BpmNodeTypeEnum;
    startTime?: Date;
    status: number;
    tasks: ApprovalTaskInfo[];
  }

  /** 流程实例 */
  export interface ProcessInstance {
    businessKey: string;
    category: string;
    createTime: string;
    endTime: string;
    fields: string[];
    formVariables: Record<string, any>;
    id: number;
    name: string;
    processDefinition?: BpmModelApi.ProcessDefinition;
    processDefinitionId: string;
    remark: string;
    result: number;
    startTime?: Date;
    startUser?: User;
    status: number;
    tasks?: BpmProcessInstanceApi.Task[];
  }

  // 审批详情
  export interface ApprovalDetail {
    activityNodes: BpmProcessInstanceApi.ApprovalNodeInfo[];
    formFieldsPermission: any;
    processDefinition: BpmModelApi.ProcessDefinition;
    processInstance: BpmProcessInstanceApi.ProcessInstance;
    status: number;
    todoTask: BpmTaskApi.Task;
  }

  // 抄送流程实例
  export interface Copy {
    activityId: string;
    activityName: string;
    createTime: number;
    createUser: User;
    id: number;
    processInstanceId: string;
    processInstanceName: string;
    processInstanceStartTime: number;
    reason: string;
    startUser: User;
    summary: {
      key: string;
      value: string;
    }[];
    taskId: string;
  }
}

/** 查询我的流程实例分页 */
export async function getProcessInstanceMyPage(params: PageParam) {
  return requestClient.get<PageResult<BpmProcessInstanceApi.ProcessInstance>>(
    '/bpm/process-instance/my-page',
    { params },
  );
}

/** 查询管理员流程实例分页 */
export async function getProcessInstanceManagerPage(params: PageParam) {
  return requestClient.get<PageResult<BpmProcessInstanceApi.ProcessInstance>>(
    '/bpm/process-instance/manager-page',
    { params },
  );
}

/** 新增流程实例 */
export async function createProcessInstance(data: any) {
  return requestClient.post<BpmProcessInstanceApi.ProcessInstance>(
    '/bpm/process-instance/create',
    data,
  );
}

/** 申请人主动取消流程实例 */
export async function cancelProcessInstanceByStartUser(
  id: number,
  reason: string,
) {
  return requestClient.delete<boolean>(
    '/bpm/process-instance/cancel-by-start-user',
    {
      data: { id, reason },
    },
  );
}

/** 管理员取消流程实例 */
export async function cancelProcessInstanceByAdmin(id: number, reason: string) {
  return requestClient.delete<boolean>(
    '/bpm/process-instance/cancel-by-admin',
    {
      data: { id, reason },
    },
  );
}

/** 查询流程实例详情 */
export async function getProcessInstance(id: number) {
  return requestClient.get<BpmProcessInstanceApi.ProcessInstance>(
    `/bpm/process-instance/get?id=${id}`,
  );
}

/** 查询复制流程实例分页 */
export async function getProcessInstanceCopyPage(params: PageParam) {
  return requestClient.get<PageResult<BpmProcessInstanceApi.ProcessInstance>>(
    '/bpm/process-instance/copy/page',
    { params },
  );
}

/** 更新流程实例 */
export async function updateProcessInstance(
  data: BpmProcessInstanceApi.ProcessInstance,
) {
  return requestClient.put<BpmProcessInstanceApi.ProcessInstance>(
    '/bpm/process-instance/update',
    data,
  );
}

/** 获取审批详情 */
export async function getApprovalDetail(params: any) {
  return requestClient.get<BpmProcessInstanceApi.ApprovalDetail>(
    `/bpm/process-instance/get-approval-detail`,
    { params },
  );
}

/** 获取下一个执行的流程节点 */
export async function getNextApprovalNodes(params: any) {
  return requestClient.get<BpmProcessInstanceApi.ApprovalNodeInfo[]>(
    `/bpm/process-instance/get-next-approval-nodes`,
    { params },
  );
}

/** 获取表单字段权限 */
export async function getFormFieldsPermission(params: any) {
  return requestClient.get<BpmProcessInstanceApi.ProcessInstance>(
    `/bpm/process-instance/get-form-fields-permission`,
    { params },
  );
}

/** 获取流程实例 BPMN 模型视图 */
export async function getProcessInstanceBpmnModelView(id: string) {
  return requestClient.get<BpmProcessInstanceApi.ProcessInstance>(
    `/bpm/process-instance/get-bpmn-model-view?id=${id}`,
  );
}
