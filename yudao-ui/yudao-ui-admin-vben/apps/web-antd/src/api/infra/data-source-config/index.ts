import { requestClient } from '#/api/request';

export namespace InfraDataSourceConfigApi {
  /** 数据源配置信息 */
  export interface DataSourceConfig {
    id?: number;
    name: string;
    url: string;
    username: string;
    password: string;
    createTime?: Date;
  }
}

/** 查询数据源配置列表 */
export function getDataSourceConfigList() {
  return requestClient.get<InfraDataSourceConfigApi.DataSourceConfig[]>(
    '/infra/data-source-config/list',
  );
}

/** 查询数据源配置详情 */
export function getDataSourceConfig(id: number) {
  return requestClient.get<InfraDataSourceConfigApi.DataSourceConfig>(
    `/infra/data-source-config/get?id=${id}`,
  );
}

/** 新增数据源配置 */
export function createDataSourceConfig(
  data: InfraDataSourceConfigApi.DataSourceConfig,
) {
  return requestClient.post('/infra/data-source-config/create', data);
}

/** 修改数据源配置 */
export function updateDataSourceConfig(
  data: InfraDataSourceConfigApi.DataSourceConfig,
) {
  return requestClient.put('/infra/data-source-config/update', data);
}

/** 删除数据源配置 */
export function deleteDataSourceConfig(id: number) {
  return requestClient.delete(`/infra/data-source-config/delete?id=${id}`);
}

/** 批量删除数据源配置 */
export function deleteDataSourceConfigList(ids: number[]) {
  return requestClient.delete(
    `/infra/data-source-config/delete-list?ids=${ids.join(',')}`,
  );
}
