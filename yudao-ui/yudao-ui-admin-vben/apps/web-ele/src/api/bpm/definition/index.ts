import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace BpmProcessDefinitionApi {
  /** 流程定义 */
  export interface ProcessDefinition {
    id: string;
    version: number;
    deploymentTime: number;
    suspensionState: number;
    modelType: number;
    modelId: string;
    formType?: number;
    bpmnXml?: string;
    simpleModel?: string;
    formFields?: string[];
  }
}

/** 查询流程定义 */
export async function getProcessDefinition(id?: string, key?: string) {
  return requestClient.get<BpmProcessDefinitionApi.ProcessDefinition>(
    '/bpm/process-definition/get',
    {
      params: { id, key },
    },
  );
}

/** 分页查询流程定义 */
export async function getProcessDefinitionPage(params: PageParam) {
  return requestClient.get<
    PageResult<BpmProcessDefinitionApi.ProcessDefinition>
  >('/bpm/process-definition/page', { params });
}

/** 查询流程定义列表 */
export async function getProcessDefinitionList(params: any) {
  return requestClient.get<BpmProcessDefinitionApi.ProcessDefinition[]>(
    '/bpm/process-definition/list',
    {
      params,
    },
  );
}

/** 查询流程定义列表（简单列表） */
export async function getSimpleProcessDefinitionList() {
  return requestClient.get<
    PageResult<BpmProcessDefinitionApi.ProcessDefinition>
  >('/bpm/process-definition/simple-list');
}
