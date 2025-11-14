import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpWarehouseApi {
  /** ERP 仓库信息 */
  export interface Warehouse {
    id?: number; // 仓库编号
    name: string; // 仓库名称
    address: string; // 仓库地址
    sort: number; // 排序
    remark: string; // 备注
    principal: string; // 负责人
    warehousePrice: number; // 仓储费，单位：元
    truckagePrice: number; // 搬运费，单位：元
    status: number; // 开启状态
    defaultStatus: boolean; // 是否默认
  }

  /** 仓库分页查询参数 */
  export interface WarehousePageParam extends PageParam {
    name?: string;
    status?: number;
  }
}

/** 查询仓库分页 */
export function getWarehousePage(params: ErpWarehouseApi.WarehousePageParam) {
  return requestClient.get<PageResult<ErpWarehouseApi.Warehouse>>(
    '/erp/warehouse/page',
    { params },
  );
}

/** 查询仓库精简列表 */
export function getWarehouseSimpleList() {
  return requestClient.get<ErpWarehouseApi.Warehouse[]>(
    '/erp/warehouse/simple-list',
  );
}

/** 查询仓库详情 */
export function getWarehouse(id: number) {
  return requestClient.get<ErpWarehouseApi.Warehouse>(
    `/erp/warehouse/get?id=${id}`,
  );
}

/** 新增仓库 */
export function createWarehouse(data: ErpWarehouseApi.Warehouse) {
  return requestClient.post('/erp/warehouse/create', data);
}

/** 修改仓库 */
export function updateWarehouse(data: ErpWarehouseApi.Warehouse) {
  return requestClient.put('/erp/warehouse/update', data);
}

/** 修改仓库默认状态 */
export function updateWarehouseDefaultStatus(
  id: number,
  defaultStatus: boolean,
) {
  return requestClient.put('/erp/warehouse/update-default-status', null, {
    params: { id, defaultStatus },
  });
}

/** 删除仓库 */
export function deleteWarehouse(id: number) {
  return requestClient.delete(`/erp/warehouse/delete?id=${id}`);
}

/** 导出仓库 Excel */
export function exportWarehouse(params: any) {
  return requestClient.download('/erp/warehouse/export-excel', { params });
}
