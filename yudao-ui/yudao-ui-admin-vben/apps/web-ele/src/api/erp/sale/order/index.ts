import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpSaleOrderApi {
  /** 销售订单信息 */
  export interface SaleOrder {
    id?: number; // 订单工单编号
    no: string; // 销售订单号
    customerId: number; // 客户编号
    accountId?: number; // 收款账户编号
    orderTime: Date; // 订单时间
    totalCount: number; // 合计数量
    totalPrice: number; // 合计金额，单位：元
    status: number; // 状态
    remark: string; // 备注
    outCount: number; // 销售出库数量
    fileUrl?: string; // 附件地址
    inCount?: number; // 采购入库数量
    returnCount: number; // 销售退货数量
    totalProductPrice?: number; // 产品金额，单位：元
    discountPercent?: number; // 优惠率，百分比
    discountPrice?: number; // 优惠金额，单位：元
    depositPrice?: number; // 定金金额，单位：元
    items?: SaleOrderItem[]; // 销售订单产品明细列表
  }

  /** 销售订单项 */
  export interface SaleOrderItem {
    id?: number; // 订单项编号
    orderId?: number; // 采购订单编号
    productId?: number; // 产品编号
    productName?: string; // 产品名称
    productBarCode?: string; // 产品条码
    productUnitId?: number; // 产品单位编号
    productUnitName?: string; // 产品单位名称
    productPrice?: number; // 产品单价，单位：元
    totalProductPrice?: number; // 产品总价，单位：元
    count?: number; // 数量
    totalPrice?: number; // 总价，单位：元
    taxPercent?: number; // 税率，百分比
    taxPrice?: number; // 税额，单位：元
    totalTaxPrice?: number; // 含税总价，单位：元
    remark?: string; // 备注
    stockCount?: number; // 库存数量（显示字段）
  }
}

/** 查询销售订单分页 */
export function getSaleOrderPage(params: PageParam) {
  return requestClient.get<PageResult<ErpSaleOrderApi.SaleOrder>>(
    '/erp/sale-order/page',
    { params },
  );
}

/** 查询销售订单详情 */
export function getSaleOrder(id: number) {
  return requestClient.get<ErpSaleOrderApi.SaleOrder>(
    `/erp/sale-order/get?id=${id}`,
  );
}

/** 查询销售订单项列表 */
export function getSaleOrderItemListByOrderId(orderId: number) {
  return requestClient.get<ErpSaleOrderApi.SaleOrderItem[]>(
    `/erp/sale-order/item/list-by-order-id?orderId=${orderId}`,
  );
}

/** 新增销售订单 */
export function createSaleOrder(data: ErpSaleOrderApi.SaleOrder) {
  return requestClient.post('/erp/sale-order/create', data);
}

/** 修改销售订单 */
export function updateSaleOrder(data: ErpSaleOrderApi.SaleOrder) {
  return requestClient.put('/erp/sale-order/update', data);
}

/** 更新销售订单的状态 */
export function updateSaleOrderStatus(id: number, status: number) {
  return requestClient.put('/erp/sale-order/update-status', null, {
    params: { id, status },
  });
}

/** 删除销售订单 */
export function deleteSaleOrder(ids: number[]) {
  return requestClient.delete('/erp/sale-order/delete', {
    params: { ids: ids.join(',') },
  });
}

/** 导出销售订单 Excel */
export function exportSaleOrder(params: any) {
  return requestClient.download('/erp/sale-order/export-excel', { params });
}
