import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AlertConfigApi {
  /** IoT 告警配置 VO */
  export interface AlertConfig {
    id?: number;
    name: string;
    description?: string;
    level?: number;
    status?: number;
    sceneRuleIds?: number[];
    receiveUserIds?: number[];
    receiveUserNames?: string;
    receiveTypes?: number[];
    createTime?: Date;
    updateTime?: Date;
  }
}

/** IoT 告警配置 */
export interface AlertConfig {
  id?: number;
  name?: string;
  description?: string;
  level?: number;
  status?: number;
  sceneRuleIds?: number[];
  receiveUserIds?: number[];
  receiveUserNames?: string;
  receiveTypes?: number[];
  createTime?: Date;
  updateTime?: Date;
}

/** 查询告警配置分页 */
export function getAlertConfigPage(params: PageParam) {
  return requestClient.get<PageResult<AlertConfigApi.AlertConfig>>(
    '/iot/alert-config/page',
    { params },
  );
}

/** 查询告警配置详情 */
export function getAlertConfig(id: number) {
  return requestClient.get<AlertConfigApi.AlertConfig>(
    `/iot/alert-config/get?id=${id}`,
  );
}

/** 查询所有告警配置列表 */
export function getAlertConfigList() {
  return requestClient.get<AlertConfigApi.AlertConfig[]>(
    '/iot/alert-config/list',
  );
}

/** 新增告警配置 */
export function createAlertConfig(data: AlertConfig) {
  return requestClient.post('/iot/alert-config/create', data);
}

/** 修改告警配置 */
export function updateAlertConfig(data: AlertConfig) {
  return requestClient.put('/iot/alert-config/update', data);
}

/** 删除告警配置 */
export function deleteAlertConfig(id: number) {
  return requestClient.delete(`/iot/alert-config/delete?id=${id}`);
}

/** 批量删除告警配置 */
export function deleteAlertConfigList(ids: number[]) {
  return requestClient.delete('/iot/alert-config/delete-list', {
    params: { ids: ids.join(',') },
  });
}

/** 启用/禁用告警配置 */
export function toggleAlertConfig(id: number, enabled: boolean) {
  return requestClient.put(`/iot/alert-config/toggle`, {
    id,
    enabled,
  });
}

/** 获取告警配置简单列表 */
export function getSimpleAlertConfigList() {
  return requestClient.get<AlertConfigApi.AlertConfig[]>(
    '/iot/alert-config/simple-list',
  );
}
