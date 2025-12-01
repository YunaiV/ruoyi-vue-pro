import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace PayNotifyApi {
  /** 支付通知任务 */
  export interface NotifyTask {
    id: number;
    appId: number;
    appName: string;
    type: number;
    dataId: number;
    status: number;
    merchantOrderId: string;
    merchantRefundId?: string;
    merchantTransferId?: string;
    lastExecuteTime: Date;
    nextNotifyTime: Date;
    notifyTimes: number;
    maxNotifyTimes: number;
    createTime: Date;
    updateTime: Date;
    logs?: any[];
  }
}

/** 获得支付通知明细 */
export function getNotifyTaskDetail(id: number) {
  return requestClient.get(`/pay/notify/get-detail?id=${id}`);
}

/** 获得支付通知分页 */
export function getNotifyTaskPage(params: PageParam) {
  return requestClient.get<PageResult<PayNotifyApi.NotifyTask>>(
    '/pay/notify/page',
    {
      params,
    },
  );
}
