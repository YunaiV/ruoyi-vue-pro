import type {
  BpmCandidateStrategyEnum,
  BpmNodeTypeEnum,
} from '@vben/constants';
import type { PageParam, PageResult } from '@vben/request';

import type { BpmTaskApi } from '../task';

import type { BpmModelApi } from '#/api/bpm/model';

import { requestClient } from '#/api/request';

export namespace BpmProcessInstanceApi {
  /** 流程实例 */
  export interface ProcessInstance {
    businessKey: string;
    category: string;
    categoryName?: string;
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
    summary?: {
      key: string;
      value: string;
    }[];
    tasks?: BpmProcessInstanceApi.Task[];
  }

  /** 流程实例的任务 */
  export interface Task {
    id: number;
    name: string;
    assigneeUser?: User;
  }

  /** 流程实例的用户信息 */
  export interface User {
    id: number;
    nickname: string;
    avatar: string;
    deptName?: string;
  }

  /** 审批详情 */
  export interface ApprovalDetailRespVO {
    activityNodes: BpmProcessInstanceApi.ApprovalNodeInfo[];
    formFieldsPermission: any;
    processDefinition: BpmModelApi.ProcessDefinition;
    processInstance: BpmProcessInstanceApi.ProcessInstance;
    status: number;
    todoTask: BpmTaskApi.Task;
  }

  /** 审批详情的节点信息 */
  export interface ApprovalNodeInfo {
    candidateStrategy?: BpmCandidateStrategyEnum;
    candidateUsers?: User[];
    endTime?: Date;
    id: string;
    name: string;
    nodeType: BpmNodeTypeEnum;
    startTime?: Date;
    status: number;
    processInstanceId?: string;
    tasks: ApprovalTaskInfo[];
  }

  /** 审批详情的节点的任务 */
  export interface ApprovalTaskInfo {
    id: number;
    assigneeUser: User;
    ownerUser: User;
    reason: string;
    signPicUrl: string;
    status: number;
  }

  /** 抄送流程实例 */
  export interface ProcessInstanceCopyRespVO {
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

  /** 流程实例的打印数据响应 */
  export interface ProcessPrintDataRespVO {
    printTemplateEnable: boolean;
    printTemplateHtml?: string;
    processInstance: ProcessInstance;
    tasks: {
      description: string;
      id: number;
      name: string;
      signPicUrl?: string;
    }[];
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
  return requestClient.get<BpmProcessInstanceApi.ApprovalDetailRespVO>(
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

/** 获取流程实例 BPMN 模型视图 */
export async function getProcessInstanceBpmnModelView(id: string) {
  return requestClient.get<BpmProcessInstanceApi.ProcessInstance>(
    `/bpm/process-instance/get-bpmn-model-view?id=${id}`,
  );
}

/** 获取流程实例打印数据 */
export async function getProcessInstancePrintData(id: string) {
  return requestClient.get<BpmProcessInstanceApi.ProcessPrintDataRespVO>(
    `/bpm/process-instance/get-print-data?processInstanceId=${id}`,
  );
}
