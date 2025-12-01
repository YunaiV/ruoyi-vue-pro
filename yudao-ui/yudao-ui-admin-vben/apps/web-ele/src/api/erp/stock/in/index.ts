import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpStockInApi {
  /** 其它入库单信息 */
  export interface StockIn {
    id?: number; // 入库编号
    no: string; // 入库单号
    supplierId: number; // 供应商编号
    supplierName?: string; // 供应商名称
    inTime: Date; // 入库时间
    totalCount: number; // 合计数量
    totalPrice: number; // 合计金额，单位：元
    status: number; // 状态
    remark: string; // 备注
    fileUrl?: string; // 附件
    productNames?: string; // 产品信息
    creatorName?: string; // 创建人
    items?: StockInItem[]; // 入库产品清单
  }

  /** 其它入库单产品信息 */
  export interface StockInItem {
    id?: number; // 编号
    warehouseId: number; // 仓库编号
    productId: number; // 产品编号
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

/** 查询其它入库单分页 */
export function getStockInPage(params: PageParam) {
  return requestClient.get<PageResult<ErpStockInApi.StockIn>>(
    '/erp/stock-in/page',
    {
      params,
    },
  );
}

/** 查询其它入库单详情 */
export function getStockIn(id: number) {
  return requestClient.get<ErpStockInApi.StockIn>(`/erp/stock-in/get?id=${id}`);
}

/** 新增其它入库单 */
export function createStockIn(data: ErpStockInApi.StockIn) {
  return requestClient.post('/erp/stock-in/create', data);
}

/** 修改其它入库单 */
export function updateStockIn(data: ErpStockInApi.StockIn) {
  return requestClient.put('/erp/stock-in/update', data);
}

/** 更新其它入库单的状态 */
export function updateStockInStatus(id: number, status: number) {
  return requestClient.put('/erp/stock-in/update-status', null, {
    params: { id, status },
  });
}

/** 删除其它入库单 */
export function deleteStockIn(ids: number[]) {
  return requestClient.delete('/erp/stock-in/delete', {
    params: {
      ids: ids.join(','),
    },
  });
}

/** 导出其它入库单 Excel */
export function exportStockIn(params: any) {
  return requestClient.download('/erp/stock-in/export-excel', {
    params,
  });
}
