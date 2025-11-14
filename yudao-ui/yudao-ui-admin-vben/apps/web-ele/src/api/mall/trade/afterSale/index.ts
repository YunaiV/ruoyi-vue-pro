import type { PageParam, PageResult } from '@vben/request';

import type { MallOrderApi } from '#/api/mall/trade/order';

import { requestClient } from '#/api/request';

export namespace MallAfterSaleApi {
  /** 商品属性 */
  export interface ProductProperty {
    /** 属性的编号 */
    propertyId?: null | number;
    /** 属性的名称 */
    propertyName?: string;
    /** 属性值的编号 */
    valueId?: null | number;
    /** 属性值的名称 */
    valueName?: string;
  }

  /** 交易售后 */
  export interface AfterSale {
    /** 售后编号，主键自增 */
    id?: null | number;
    /** 售后单号 */
    no?: string;
    /** 退款状态 */
    status?: null | number;
    /** 售后方式 */
    way?: null | number;
    /** 售后类型 */
    type?: null | number;
    /** 用户编号 */
    userId?: null | number;
    /** 申请原因 */
    applyReason?: string;
    /** 补充描述 */
    applyDescription?: string;
    /** 补充凭证图片 */
    applyPicUrls?: string[];
    /** 交易订单编号 */
    orderId?: null | number;
    /** 订单流水号 */
    orderNo?: string;
    /** 交易订单项编号 */
    orderItemId?: null | number;
    /** 商品 SPU 编号 */
    spuId?: null | number;
    /** 商品 SPU 名称 */
    spuName?: string;
    /** 商品 SKU 编号 */
    skuId?: null | number;
    /** 属性数组 */
    properties?: ProductProperty[];
    /** 商品图片 */
    picUrl?: string;
    /** 退货商品数量 */
    count?: null | number;
    /** 审批时间 */
    auditTime?: Date;
    /** 审批人 */
    auditUserId?: null | number;
    /** 审批备注 */
    auditReason?: string;
    /** 退款金额，单位：分 */
    refundPrice?: null | number;
    /** 支付退款编号 */
    payRefundId?: null | number;
    /** 退款时间 */
    refundTime?: Date;
    /** 退货物流公司编号 */
    logisticsId?: null | number;
    /** 退货物流单号 */
    logisticsNo?: string;
    /** 退货时间 */
    deliveryTime?: Date;
    /** 收货时间 */
    receiveTime?: Date;
    /** 收货备注 */
    receiveReason?: string;
    order?: MallOrderApi.Order; // 关联订单
    orderItem?: MallOrderApi.OrderItem; // 关联订单项
    logs?: any[]; // 关联售后日志
  }

  /** 拒绝售后请求 */
  export interface DisagreeRequest {
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
export function disagreeAfterSale(data: MallAfterSaleApi.DisagreeRequest) {
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
