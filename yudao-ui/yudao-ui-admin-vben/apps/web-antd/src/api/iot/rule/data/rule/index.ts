import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace DataRuleApi {
  /** IoT 数据流转规则 VO */
  export interface Rule {
    id?: number;
    name: string;
    description?: string;
    status?: number;
    productId?: number;
    productKey?: string;
    sourceConfigs?: SourceConfig[];
    sinkIds?: number[];
    createTime?: Date;
  }

  /** IoT 数据源配置 */
  export interface SourceConfig {
    productId?: number;
    productKey?: string;
    deviceId?: number;
    type?: string;
    topic?: string;
  }
}

/** IoT 数据流转规则 */
export interface DataRule {
  id?: number;
  name?: string;
  description?: string;
  status?: number;
  productId?: number;
  productKey?: string;
  sourceConfigs?: any[];
  sinkIds?: number[];
  createTime?: Date;
}

/** 查询数据流转规则分页 */
export function getDataRulePage(params: PageParam) {
  return requestClient.get<PageResult<DataRuleApi.Rule>>(
    '/iot/data-rule/page',
    { params },
  );
}

/** 查询数据流转规则详情 */
export function getDataRule(id: number) {
  return requestClient.get<DataRuleApi.Rule>(`/iot/data-rule/get?id=${id}`);
}

/** 新增数据流转规则 */
export function createDataRule(data: DataRule) {
  return requestClient.post('/iot/data-rule/create', data);
}

/** 修改数据流转规则 */
export function updateDataRule(data: DataRule) {
  return requestClient.put('/iot/data-rule/update', data);
}

/** 删除数据流转规则 */
export function deleteDataRule(id: number) {
  return requestClient.delete(`/iot/data-rule/delete?id=${id}`);
}

/** 批量删除数据流转规则 */
export function deleteDataRuleList(ids: number[]) {
  return requestClient.delete('/iot/data-rule/delete-list', {
    params: { ids: ids.join(',') },
  });
}

/** 更新数据流转规则状态 */
export function updateDataRuleStatus(id: number, status: number) {
  return requestClient.put(`/iot/data-rule/update-status`, {
    id,
    status,
  });
}
