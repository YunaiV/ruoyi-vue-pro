import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpStockCheckApi {
  /** 库存盘点单信息 */
  export interface StockCheck {
    id?: number; // 盘点编号
    no: string; // 盘点单号
    checkTime: Date; // 盘点时间
    totalCount: number; // 合计数量
    totalPrice: number; // 合计金额，单位：元
    status: number; // 状态
    remark: string; // 备注
    fileUrl?: string; // 附件
    productNames?: string; // 产品信息
    creatorName?: string; // 创建人
    items?: StockCheckItem[]; // 盘点产品清单
  }

  /** 库存盘点项 */
  export interface StockCheckItem {
    id?: number; // 编号
    warehouseId?: number; // 仓库编号
    productId?: number; // 产品编号
    productName?: string; // 产品名称
    productUnitId?: number; // 产品单位编号
    productUnitName?: string; // 产品单位名称
    productBarCode?: string; // 产品条码
    count?: number; // 盈亏数量
    actualCount?: number; // 实际库存
    productPrice?: number; // 产品单价
    totalPrice?: number; // 总价
    stockCount?: number; // 账面库存
    remark?: string; // 备注
  }
}

/** 查询库存盘点单分页 */
export function getStockCheckPage(params: PageParam) {
  return requestClient.get<PageResult<ErpStockCheckApi.StockCheck>>(
    '/erp/stock-check/page',
    {
      params,
    },
  );
}

/** 查询库存盘点单详情 */
export function getStockCheck(id: number) {
  return requestClient.get<ErpStockCheckApi.StockCheck>(
    `/erp/stock-check/get?id=${id}`,
  );
}

/** 新增库存盘点单 */
export function createStockCheck(data: ErpStockCheckApi.StockCheck) {
  return requestClient.post('/erp/stock-check/create', data);
}

/** 修改库存盘点单 */
export function updateStockCheck(data: ErpStockCheckApi.StockCheck) {
  return requestClient.put('/erp/stock-check/update', data);
}

/** 更新库存盘点单的状态 */
export function updateStockCheckStatus(id: number, status: number) {
  return requestClient.put('/erp/stock-check/update-status', null, {
    params: { id, status },
  });
}

/** 删除库存盘点 */
export function deleteStockCheck(ids: number[]) {
  return requestClient.delete('/erp/stock-check/delete', {
    params: {
      ids: ids.join(','),
    },
  });
}

/** 导出库存盘点单 Excel */
export function exportStockCheck(params: any) {
  return requestClient.download('/erp/stock-check/export-excel', {
    params,
  });
}
