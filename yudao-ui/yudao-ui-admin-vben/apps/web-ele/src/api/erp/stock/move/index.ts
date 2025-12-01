import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpStockMoveApi {
  /** 库存调拨单信息 */
  export interface StockMove {
    id?: number; // 调拨编号
    no: string; // 调拨单号
    outTime: Date; // 调拨时间
    totalCount: number; // 合计数量
    totalPrice: number; // 合计金额，单位：元
    status: number; // 状态
    remark: string; // 备注
    fileUrl?: string; // 附件
    fromWarehouseId?: number; // 来源仓库编号
    createTime: Date; // 创建时间
    creator: string; // 创建人
    creatorName: string; // 创建人名称
    productNames: string; // 产品名称
    items?: StockMoveItem[]; // 子表信息
  }

  /** 库存调拨单子表信息 */
  export interface StockMoveItem {
    count: number; // 数量
    fromWarehouseId?: number; // 来源仓库ID
    id?: number; // ID
    productBarCode: string; // 产品条形码
    productId?: number; // 产品ID
    productName?: string; // 产品名称
    productPrice: number; // 产品单价
    productUnitName?: string; // 产品单位
    remark?: string; // 备注
    stockCount: number; // 库存数量
    toWarehouseId?: number; // 目标仓库ID
    totalPrice?: number; // 总价
  }
}

/** 查询库存调拨单分页 */
export function getStockMovePage(params: PageParam) {
  return requestClient.get<PageResult<ErpStockMoveApi.StockMove>>(
    '/erp/stock-move/page',
    {
      params,
    },
  );
}

/** 查询库存调拨单详情 */
export function getStockMove(id: number) {
  return requestClient.get<ErpStockMoveApi.StockMove>(
    `/erp/stock-move/get?id=${id}`,
  );
}

/** 新增库存调拨单 */
export function createStockMove(data: ErpStockMoveApi.StockMove) {
  return requestClient.post('/erp/stock-move/create', data);
}

/** 修改库存调拨单 */
export function updateStockMove(data: ErpStockMoveApi.StockMove) {
  return requestClient.put('/erp/stock-move/update', data);
}

/** 更新库存调拨单的状态 */
export function updateStockMoveStatus(id: number, status: number) {
  return requestClient.put('/erp/stock-move/update-status', null, {
    params: { id, status },
  });
}

/** 删除库存调拨单 */
export function deleteStockMove(ids: number[]) {
  return requestClient.delete('/erp/stock-move/delete', {
    params: {
      ids: ids.join(','),
    },
  });
}

/** 导出库存调拨单 Excel */
export function exportStockMove(params: any) {
  return requestClient.download('/erp/stock-move/export-excel', { params });
}
