import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace DemoOrderApi {
  /** 示例订单信息 */
  export interface Order {
    id?: number;
    userId?: number;
    spuName?: string;
    price?: number;
    payStatus?: boolean;
    payOrderId?: number;
    payTime?: Date;
    payChannelCode?: string;
    payRefundId?: number;
    refundPrice?: number;
    refundTime?: Date;
    spuId?: number;
    createTime?: Date;
  }
}

/** 创建示例订单 */
export function createDemoOrder(data: DemoOrderApi.Order) {
  return requestClient.post('/pay/demo-order/create', data);
}

/** 获得示例订单分页 */
export function getDemoOrderPage(params: PageParam) {
  return requestClient.get<PageResult<DemoOrderApi.Order>>(
    '/pay/demo-order/page',
    {
      params,
    },
  );
}

/** 退款示例订单 */
export function refundDemoOrder(id: number) {
  return requestClient.put(`/pay/demo-order/refund?id=${id}`);
}
