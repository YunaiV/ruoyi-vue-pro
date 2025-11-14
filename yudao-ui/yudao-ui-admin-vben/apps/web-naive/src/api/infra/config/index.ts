import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace InfraConfigApi {
  /** 参数配置信息 */
  export interface Config {
    id?: number;
    category: string;
    name: string;
    key: string;
    value: string;
    type: number;
    visible: boolean;
    remark: string;
    createTime?: Date;
  }
}

/** 查询参数列表 */
export function getConfigPage(params: PageParam) {
  return requestClient.get<PageResult<InfraConfigApi.Config>>(
    '/infra/config/page',
    {
      params,
    },
  );
}

/** 查询参数详情 */
export function getConfig(id: number) {
  return requestClient.get<InfraConfigApi.Config>(`/infra/config/get?id=${id}`);
}

/** 根据参数键名查询参数值 */
export function getConfigKey(configKey: string) {
  return requestClient.get<string>(
    `/infra/config/get-value-by-key?key=${configKey}`,
  );
}

/** 新增参数 */
export function createConfig(data: InfraConfigApi.Config) {
  return requestClient.post('/infra/config/create', data);
}

/** 修改参数 */
export function updateConfig(data: InfraConfigApi.Config) {
  return requestClient.put('/infra/config/update', data);
}

/** 删除参数 */
export function deleteConfig(id: number) {
  return requestClient.delete(`/infra/config/delete?id=${id}`);
}

/** 批量删除参数 */
export function deleteConfigList(ids: number[]) {
  return requestClient.delete(`/infra/config/delete-list?ids=${ids.join(',')}`);
}

/** 导出参数 */
export function exportConfig(params: any) {
  return requestClient.download('/infra/config/export-excel', {
    params,
  });
}
