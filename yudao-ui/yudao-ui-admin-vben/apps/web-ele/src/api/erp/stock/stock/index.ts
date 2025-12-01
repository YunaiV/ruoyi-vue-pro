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

  /** 产品库存查询参数 */
  export interface StockQueryReqVO {
    productId: number;
    warehouseId: number;
  }
}

/** 查询产品库存分页 */
export function getStockPage(params: PageParam) {
  return requestClient.get<PageResult<ErpStockApi.Stock>>('/erp/stock/page', {
    params,
  });
}

/** 获得产品库存数量 */
export function getStockCount(productId: number, warehouseId?: number) {
  const params: any = { productId };
  if (warehouseId !== undefined) {
    params.warehouseId = warehouseId;
  }
  return requestClient.get<number>('/erp/stock/get-count', {
    params,
  });
}

/** 导出产品库存 Excel */
export function exportStock(params: any) {
  return requestClient.download('/erp/stock/export-excel', {
    params,
  });
}

/** 获取库存数量 */
export function getWarehouseStockCount(params: ErpStockApi.StockQueryReqVO) {
  return requestClient.get<number>('/erp/stock/get-count', {
    params,
  });
}
