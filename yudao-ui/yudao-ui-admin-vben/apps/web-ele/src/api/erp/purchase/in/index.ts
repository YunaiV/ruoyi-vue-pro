import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpPurchaseInApi {
  /** 采购入库信息 */
  export interface PurchaseIn {
    id?: number; // 入库工单编号
    no?: string; // 采购入库号
    supplierId?: number; // 供应商编号
    inTime?: Date; // 入库时间
    totalCount?: number; // 合计数量
    totalPrice?: number; // 合计金额，单位：元
    status?: number; // 状态
    remark?: string; // 备注
    outCount?: number; // 采购出库数量
    returnCount?: number; // 采购退货数量
    discountPercent?: number; // 折扣百分比
    discountPrice?: number; // 折扣金额
    paymentPrice?: number; // 实际支付金额
    otherPrice?: number; // 其他费用
    totalProductPrice?: number; // 合计商品金额
    taxPrice?: number; // 合计税额
    items?: PurchaseInItem[]; // 采购入库明细
  }

  /** 采购项信息 */
  export interface PurchaseInItem {
    count?: number;
    id?: number;
    orderItemId?: number;
    productBarCode?: string;
    productId?: number;
    productName: string;
    productPrice: number;
    productUnitId?: number;
    productUnitName?: string;
    totalProductPrice?: number;
    remark: string;
    stockCount?: number;
    taxPercent?: number;
    taxPrice?: number;
    totalPrice?: number;
    warehouseId?: number;
    inCount?: number;
  }
}

/** 查询采购入库分页 */
export function getPurchaseInPage(params: PageParam) {
  return requestClient.get<PageResult<ErpPurchaseInApi.PurchaseIn>>(
    '/erp/purchase-in/page',
    {
      params,
    },
  );
}

/** 查询采购入库详情 */
export function getPurchaseIn(id: number) {
  return requestClient.get<ErpPurchaseInApi.PurchaseIn>(
    `/erp/purchase-in/get?id=${id}`,
  );
}

/** 新增采购入库 */
export function createPurchaseIn(data: ErpPurchaseInApi.PurchaseIn) {
  return requestClient.post('/erp/purchase-in/create', data);
}

/** 修改采购入库 */
export function updatePurchaseIn(data: ErpPurchaseInApi.PurchaseIn) {
  return requestClient.put('/erp/purchase-in/update', data);
}

/** 更新采购入库的状态 */
export function updatePurchaseInStatus(id: number, status: number) {
  return requestClient.put('/erp/purchase-in/update-status', null, {
    params: {
      id,
      status,
    },
  });
}

/** 删除采购入库 */
export function deletePurchaseIn(ids: number[]) {
  return requestClient.delete('/erp/purchase-in/delete', {
    params: {
      ids: ids.join(','),
    },
  });
}

/** 导出采购入库 Excel */
export function exportPurchaseIn(params: any) {
  return requestClient.download('/erp/purchase-in/export-excel', {
    params,
  });
}
