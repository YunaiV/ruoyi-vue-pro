import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace RuleSceneApi {
  /** 场景联动规则 */
  export interface SceneRule {
    id?: number;
    name: string;
    description?: string;
    status?: number;
    triggers?: Trigger[];
    actions?: Action[];
    createTime?: Date;
  }

  /** 场景联动规则的触发器 */
  export interface Trigger {
    type?: string;
    productId?: number;
    deviceId?: number;
    identifier?: string;
    operator?: string;
    value?: any;
    cronExpression?: string;
    conditionGroups?: TriggerConditionGroup[];
  }

  /** 场景联动规则的触发条件组 */
  export interface TriggerConditionGroup {
    conditions?: TriggerCondition[];
    operator?: string;
  }

  /**  场景联动规则的触发条件 */
  export interface TriggerCondition {
    productId?: number;
    deviceId?: number;
    identifier?: string;
    operator?: string;
    value?: any;
    type?: string;
  }

  /** 场景联动规则的动作 */
  export interface Action {
    type?: string;
    productId?: number;
    deviceId?: number;
    identifier?: string;
    value?: any;
    alertConfigId?: number;
  }
}

// TODO @haohao：貌似下面的，和 RuleSceneApi 重复了。
/** IoT 场景联动规则 */
export interface IotSceneRule {
  id?: number;
  name?: string;
  description?: string;
  status?: number;
  triggers?: Trigger[];
  actions?: Action[];
  createTime?: Date;
}

/** IoT 场景联动规则触发器 */
export interface Trigger {
  type?: string;
  productId?: number;
  deviceId?: number;
  identifier?: string;
  operator?: string;
  value?: any;
  cronExpression?: string;
  conditionGroups?: TriggerConditionGroup[];
}

/** IoT 场景联动规则触发条件组 */
export interface TriggerConditionGroup {
  conditions?: TriggerCondition[];
  operator?: string;
}

/** IoT 场景联动规则触发条件 */
export interface TriggerCondition {
  productId?: number;
  deviceId?: number;
  identifier?: string;
  operator?: string;
  value?: any;
  type?: string;
  param?: string;
}

/** IoT 场景联动规则动作 */
export interface Action {
  type?: string;
  productId?: number;
  deviceId?: number;
  identifier?: string;
  value?: any;
  alertConfigId?: number;
  params?: string;
}

/** 查询场景联动规则分页 */
export function getSceneRulePage(params: PageParam) {
  return requestClient.get<PageResult<RuleSceneApi.SceneRule>>(
    '/iot/scene-rule/page',
    { params },
  );
}

/** 查询场景联动规则详情 */
export function getSceneRule(id: number) {
  return requestClient.get<RuleSceneApi.SceneRule>(
    `/iot/scene-rule/get?id=${id}`,
  );
}

/** 新增场景联动规则 */
export function createSceneRule(data: IotSceneRule) {
  return requestClient.post('/iot/scene-rule/create', data);
}

/** 修改场景联动规则 */
export function updateSceneRule(data: IotSceneRule) {
  return requestClient.put('/iot/scene-rule/update', data);
}

/** 删除场景联动规则 */
export function deleteSceneRule(id: number) {
  return requestClient.delete(`/iot/scene-rule/delete?id=${id}`);
}

/** 批量删除场景联动规则 */
// TODO @haohao：貌似用上。
export function deleteSceneRuleList(ids: number[]) {
  return requestClient.delete('/iot/scene-rule/delete-list', {
    params: { ids: ids.join(',') },
  });
}

/** 更新场景联动规则状态 */
export function updateSceneRuleStatus(id: number, status: number) {
  return requestClient.put(`/iot/scene-rule/update-status`, {
    id,
    status,
  });
}

/** 获取场景联动规则简单列表 */
export function getSimpleRuleSceneList() {
  return requestClient.get<RuleSceneApi.SceneRule[]>(
    '/iot/scene-rule/simple-list',
  );
}
