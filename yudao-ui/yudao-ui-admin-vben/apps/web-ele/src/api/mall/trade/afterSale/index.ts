import type { PageParam, PageResult } from '@vben/request';

import type { MallOrderApi } from '#/api/mall/trade/order';

import { requestClient } from '#/api/request';

export namespace MallAfterSaleApi {
  /** 交易售后 */
  export interface AfterSale {
    id?: number; // 售后编号
    no?: string; // 售后单号
    status?: number; // 退款状态
    way?: number; // 售后方式
    type?: number; // 售后类型
    userId?: number; // 用户编号
    applyReason?: string; // 申请原因
    applyDescription?: string; // 补充描述
    applyPicUrls?: string[]; // 补充凭证图片
    orderId?: number; // 交易订单编号
    orderNo?: string; // 订单流水号
    orderItemId?: number; // 交易订单项编号
    spuId?: number; // 商品 SPU 编号
    spuName?: string; // 商品 SPU 名称
    skuId?: number; // 商品 SKU 编号
    properties?: {
      propertyId?: number; // 属性的编号
      propertyName?: string; // 属性的名称
      valueId?: number; // 属性值的编号
      valueName?: string; // 属性值的名称
    }[]; // 属性数组
    picUrl?: string; // 商品图片
    count?: number; // 退货商品数量
    auditTime?: Date; // 审批时间
    auditUserId?: number; // 审批人
    auditReason?: string; // 审批备注
    refundPrice?: number; // 退款金额，单位：分
    payRefundId?: number; // 支付退款编号
    refundTime?: Date; // 退款时间
    logisticsId?: number; // 退货物流公司编号
    logisticsNo?: string; // 退货物流单号
    deliveryTime?: Date; // 退货时间
    receiveTime?: Date; // 收货时间
    receiveReason?: string; // 收货备注
    order?: MallOrderApi.Order; // 关联订单
    orderItem?: MallOrderApi.OrderItem; // 关联订单项
    logs?: any[]; // 关联售后日志
  }

  /** 拒绝售后请求 */
  export interface AfterSaleDisagreeReqVO {
    /** 售后编号 */
    id: number;
    /** 拒绝原因 */
    reason: string;
  }
}

/** 获得交易售后分页 */
export function getAfterSalePage(params: PageParam) {
  return requestClient.get<PageResult<MallAfterSaleApi.AfterSale>>(
    '/trade/after-sale/page',
    { params },
  );
}

/** 获得交易售后详情 */
export function getAfterSale(id: number) {
  return requestClient.get<MallAfterSaleApi.AfterSale>(
    `/trade/after-sale/get-detail?id=${id}`,
  );
}

/** 同意售后 */
export function agreeAfterSale(id: number) {
  return requestClient.put(`/trade/after-sale/agree?id=${id}`);
}

/** 拒绝售后 */
export function disagreeAfterSale(
  data: MallAfterSaleApi.AfterSaleDisagreeReqVO,
) {
  return requestClient.put('/trade/after-sale/disagree', data);
}

/** 确认收货 */
export function receiveAfterSale(id: number) {
  return requestClient.put(`/trade/after-sale/receive?id=${id}`);
}

/** 拒绝收货 */
export function refuseAfterSale(id: number) {
  return requestClient.put(`/trade/after-sale/refuse?id=${id}`);
}

/** 确认退款 */
export function refundAfterSale(id: number) {
  return requestClient.put(`/trade/after-sale/refund?id=${id}`);
}
