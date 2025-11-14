import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpStockRecordApi {
  /** ERP 产品库存明细 */
  export interface StockRecord {
    id?: number; // 编号
    productId: number; // 产品编号
    warehouseId: number; // 仓库编号
    count: number; // 出入库数量
    totalCount: number; // 总库存量
    bizType: number; // 业务类型
    bizId: number; // 业务编号
    bizItemId: number; // 业务项编号
    bizNo: string; // 业务单号
  }

  /** 库存记录分页查询参数 */
  export interface StockRecordPageParam extends PageParam {
    productId?: number;
    warehouseId?: number;
    bizType?: number;
  }
}

/** 查询产品库存明细分页 */
export function getStockRecordPage(
  params: ErpStockRecordApi.StockRecordPageParam,
) {
  return requestClient.get<PageResult<ErpStockRecordApi.StockRecord>>(
    '/erp/stock-record/page',
    { params },
  );
}

/** 查询产品库存明细详情 */
export function getStockRecord(id: number) {
  return requestClient.get<ErpStockRecordApi.StockRecord>(
    `/erp/stock-record/get?id=${id}`,
  );
}

/** 导出产品库存明细 Excel */
export function exportStockRecord(params: any) {
  return requestClient.download('/erp/stock-record/export-excel', { params });
}
