import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace PayOrderApi {
  /** 支付订单信息 */
  export interface Order {
    id: number;
    no: string;
    price: number;
    channelFeePrice: number;
    refundPrice: number;
    merchantId: number;
    appId: number;
    appName: string;
    channelId: number;
    channelCode: string;
    merchantOrderId: string;
    subject: string;
    body: string;
    notifyUrl: string;
    notifyStatus: number;
    amount: number;
    channelFeeRate: number;
    channelFeeAmount: number;
    status: number;
    userIp: string;
    expireTime: Date;
    successTime: Date;
    notifyTime: Date;
    successExtensionId: number;
    refundStatus: number;
    refundTimes: number;
    refundAmount: number;
    channelUserId: string;
    channelOrderNo: string;
    channelNotifyData: string;
    createTime: Date;
    updateTime: Date;
  }
}

/** 查询支付订单列表 */
export function getOrderPage(params: PageParam) {
  return requestClient.get<PageResult<PayOrderApi.Order>>('/pay/order/page', {
    params,
  });
}

/** 查询支付订单详情 */
export function getOrder(id: number, sync?: boolean) {
  return requestClient.get<PayOrderApi.Order>('/pay/order/get', {
    params: {
      id,
      sync,
    },
  });
}

/** 获得支付订单的明细 */
export function getOrderDetail(id: number) {
  return requestClient.get<PayOrderApi.Order>(`/pay/order/get-detail?id=${id}`);
}

/** 提交支付订单 */
export function submitOrder(data: any) {
  return requestClient.post('/pay/order/submit', data);
}

/** 导出支付订单 */
export function exportOrder(params: any) {
  return requestClient.download('/pay/order/export-excel', { params });
}
