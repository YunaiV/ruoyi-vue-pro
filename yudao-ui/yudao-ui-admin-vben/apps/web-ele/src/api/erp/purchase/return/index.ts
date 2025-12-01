import type { PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpPurchaseReturnApi {
  /** 采购退货信息 */
  export interface PurchaseReturn {
    id?: number; // 采购退货编号
    no?: string; // 采购退货号
    supplierId?: number; // 供应商编号
    returnTime?: Date; // 退货时间
    totalCount?: number; // 合计数量
    totalPrice: number; // 合计金额，单位：元
    discountPercent?: number; // 折扣百分比
    discountPrice?: number; // 折扣金额
    status?: number; // 状态
    remark?: string; // 备注
    totalTaxPrice?: number; // 合计税额
    otherPrice?: number; // 其他费用
    items?: PurchaseReturnItem[];
  }

  /** 采购退货项 */
  export interface PurchaseReturnItem {
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
  }
}

/** 查询采购退货分页 */
export function getPurchaseReturnPage(params: any) {
  return requestClient.get<PageResult<ErpPurchaseReturnApi.PurchaseReturn>>(
    '/erp/purchase-return/page',
    {
      params,
    },
  );
}

/** 查询采购退货详情 */
export function getPurchaseReturn(id: number) {
  return requestClient.get<ErpPurchaseReturnApi.PurchaseReturn>(
    `/erp/purchase-return/get?id=${id}`,
  );
}

/** 新增采购退货 */
export function createPurchaseReturn(
  data: ErpPurchaseReturnApi.PurchaseReturn,
) {
  return requestClient.post('/erp/purchase-return/create', data);
}

/** 修改采购退货 */
export function updatePurchaseReturn(
  data: ErpPurchaseReturnApi.PurchaseReturn,
) {
  return requestClient.put('/erp/purchase-return/update', data);
}

/** 更新采购退货的状态 */
export function updatePurchaseReturnStatus(id: number, status: number) {
  return requestClient.put('/erp/purchase-return/update-status', null, {
    params: { id, status },
  });
}

/** 删除采购退货 */
export function deletePurchaseReturn(ids: number[]) {
  return requestClient.delete('/erp/purchase-return/delete', {
    params: {
      ids: ids.join(','),
    },
  });
}

/** 导出采购退货 Excel */
export function exportPurchaseReturn(params: any) {
  return requestClient.download('/erp/purchase-return/export-excel', {
    params,
  });
}
