import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpStockOutApi {
  /** 其它出库单信息 */
  export interface StockOut {
    id?: number; // 出库编号
    no: string; // 出库单号
    customerId: number; // 客户编号
    outTime: Date; // 出库时间
    totalCount: number; // 合计数量
    totalPrice: number; // 合计金额，单位：元
    status: number; // 状态
    remark: string; // 备注
    fileUrl?: string; // 附件
    items?: StockOutItem[]; // 出库产品清单
  }

  /** 其它出库单产品信息 */
  export interface StockOutItem {
    id?: number; // 编号
    warehouseId?: number; // 仓库编号
    productId?: number; // 产品编号
    productName?: string; // 产品名称
    productUnitId?: number; // 产品单位编号
    productUnitName?: string; // 产品单位名称
    productBarCode?: string; // 产品条码
    count: number; // 数量
    productPrice: number; // 产品单价
    totalPrice: number; // 总价
    stockCount?: number; // 库存数量
    remark?: string; // 备注
  }
}

/** 查询其它出库单分页 */
export function getStockOutPage(params: PageParam) {
  return requestClient.get<PageResult<ErpStockOutApi.StockOut>>(
    '/erp/stock-out/page',
    {
      params,
    },
  );
}

/** 查询其它出库单详情 */
export function getStockOut(id: number) {
  return requestClient.get<ErpStockOutApi.StockOut>(
    `/erp/stock-out/get?id=${id}`,
  );
}

/** 新增其它出库单 */
export function createStockOut(data: ErpStockOutApi.StockOut) {
  return requestClient.post('/erp/stock-out/create', data);
}

/** 修改其它出库单 */
export function updateStockOut(data: ErpStockOutApi.StockOut) {
  return requestClient.put('/erp/stock-out/update', data);
}

/** 更新其它出库单的状态 */
export function updateStockOutStatus(id: number, status: number) {
  return requestClient.put('/erp/stock-out/update-status', null, {
    params: { id, status },
  });
}

/** 删除其它出库单 */
export function deleteStockOut(ids: number[]) {
  return requestClient.delete('/erp/stock-out/delete', {
    params: {
      ids: ids.join(','),
    },
  });
}

/** 导出其它出库单 Excel */
export function exportStockOut(params: any) {
  return requestClient.download('/erp/stock-out/export-excel', {
    params,
  });
}
