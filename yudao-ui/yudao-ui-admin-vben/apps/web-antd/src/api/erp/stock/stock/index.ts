import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpStockApi {
  /** 产品库存信息 */
  export interface Stock {
    id?: number; // 编号
    productId: number; // 产品编号
    warehouseId: number; // 仓库编号
    count: number; // 库存数量
  }

  /** 产品库存分页查询参数 */
  export interface StockPageParams extends PageParam {
    productId?: number;
    warehouseId?: number;
  }

  /** 产品库存查询参数 */
  export interface StockQueryParams {
    productId: number;
    warehouseId: number;
  }
}

/**
 * 查询产品库存分页
 */
export function getStockPage(params: ErpStockApi.StockPageParams) {
  return requestClient.get<PageResult<ErpStockApi.Stock>>('/erp/stock/page', {
    params,
  });
}

/**
 * 查询产品库存详情
 */
export function getStock(id: number) {
  return requestClient.get<ErpStockApi.Stock>(`/erp/stock/get?id=${id}`);
}

/**
 * 根据产品和仓库查询库存详情
 */
export function getStockByProductAndWarehouse(
  params: ErpStockApi.StockQueryParams,
) {
  return requestClient.get<ErpStockApi.Stock>('/erp/stock/get', {
    params,
  });
}

/**
 * 获得产品库存数量
 */
export function getStockCount(productId: number, warehouseId?: number) {
  const params: any = { productId };
  if (warehouseId !== undefined) {
    params.warehouseId = warehouseId;
  }
  return requestClient.get<number>('/erp/stock/get-count', {
    params,
  });
}

/**
 * 导出产品库存 Excel
 */
export function exportStock(params: ErpStockApi.StockPageParams) {
  return requestClient.download('/erp/stock/export-excel', {
    params,
  });
}

/**
 * 获取库存数量
 */
export function getWarehouseStockCount(params: ErpStockApi.StockQueryParams) {
  return requestClient.get<number>('/erp/stock/get-count', {
    params,
  });
}
