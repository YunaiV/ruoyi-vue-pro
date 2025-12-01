import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace PayRefundApi {
  /** 退款订单信息 */
  export interface Refund {
    id: number;
    merchantId: number;
    appId: number;
    channelId: number;
    channelCode: string;
    orderId: string;
    tradeNo: string;
    merchantOrderId: string;
    merchantRefundNo: string;
    merchantRefundId: string;
    notifyUrl: string;
    notifyStatus: number;
    status: number;
    payPrice: number;
    refundPrice: number;
    type: number;
    payAmount: number;
    refundAmount: number;
    reason: string;
    userIp: string;
    channelOrderNo: string;
    channelRefundNo: string;
    channelErrorCode: string;
    channelErrorMsg: string;
    channelExtras: string;
    expireTime: Date;
    successTime: Date;
    notifyTime: Date;
    createTime: Date;
    updateTime: Date;
  }
}

/** 查询退款订单列表 */
export function getRefundPage(params: PageParam) {
  return requestClient.get<PageResult<PayRefundApi.Refund>>(
    '/pay/refund/page',
    {
      params,
    },
  );
}

/** 查询退款订单详情 */
export function getRefund(id: number) {
  return requestClient.get<PayRefundApi.Refund>(`/pay/refund/get?id=${id}`);
}

/** 导出退款订单 */
export function exportRefund(params: any) {
  return requestClient.download('/pay/refund/export-excel', { params });
}
