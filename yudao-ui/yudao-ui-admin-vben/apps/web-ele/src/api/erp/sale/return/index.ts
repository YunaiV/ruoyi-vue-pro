import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpSaleReturnApi {
  /** 销售退货信息 */
  export interface SaleReturn {
    id?: number; // 销售退货编号
    no?: string; // 销售退货号
    customerId?: number; // 客户编号
    returnTime?: Date; // 退货时间
    totalCount?: number; // 合计数量
    totalPrice?: number; // 合计金额，单位：元
    status?: number; // 状态
    remark?: string; // 备注
    discountPercent?: number; // 折扣百分比
    discountPrice?: number; // 折扣金额
    otherPrice?: number; // 其他费用
    totalProductPrice?: number; // 合计商品金额
    taxPrice?: number; // 合计税额
    totalTaxPrice?: number; // 合计税额
    fileUrl?: string; // 附件地址
    items?: SaleReturnItem[];
  }

  /** 销售退货项 */
  export interface SaleReturnItem {
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
    returnCount?: number;
  }
}

/** 查询销售退货分页 */
export function getSaleReturnPage(params: PageParam) {
  return requestClient.get<PageResult<ErpSaleReturnApi.SaleReturn>>(
    '/erp/sale-return/page',
    {
      params,
    },
  );
}

/** 查询销售退货详情 */
export function getSaleReturn(id: number) {
  return requestClient.get<ErpSaleReturnApi.SaleReturn>(
    `/erp/sale-return/get?id=${id}`,
  );
}

/** 新增销售退货 */
export function createSaleReturn(data: ErpSaleReturnApi.SaleReturn) {
  return requestClient.post('/erp/sale-return/create', data);
}

/** 修改销售退货 */
export function updateSaleReturn(data: ErpSaleReturnApi.SaleReturn) {
  return requestClient.put('/erp/sale-return/update', data);
}

/** 更新销售退货的状态 */
export function updateSaleReturnStatus(id: number, status: number) {
  return requestClient.put('/erp/sale-return/update-status', null, {
    params: { id, status },
  });
}

/** 删除销售退货 */
export function deleteSaleReturn(ids: number[]) {
  return requestClient.delete('/erp/sale-return/delete', {
    params: {
      ids: ids.join(','),
    },
  });
}

/** 导出销售退货 Excel */
export function exportSaleReturn(params: any) {
  return requestClient.download('/erp/sale-return/export-excel', {
    params,
  });
}
