import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace PayAppApi {
  /** 支付应用信息 */
  export interface App {
    id?: number;
    appKey: string;
    name: string;
    status: number;
    remark: string;
    payNotifyUrl: string;
    refundNotifyUrl: string;
    transferNotifyUrl: string;
    merchantId: number;
    merchantName: string;
    createTime?: Date;
    channelCodes?: string[];
  }

  /** 更新状态请求 */
  export interface AppUpdateStatusReqVO {
    id: number;
    status: number;
  }
}

/** 查询支付应用列表 */
export function getAppPage(params: PageParam) {
  return requestClient.get<PageResult<PayAppApi.App>>('/pay/app/page', {
    params,
  });
}

/** 查询支付应用详情 */
export function getApp(id: number) {
  return requestClient.get<PayAppApi.App>(`/pay/app/get?id=${id}`);
}

/** 新增支付应用 */
export function createApp(data: PayAppApi.App) {
  return requestClient.post('/pay/app/create', data);
}

/** 修改支付应用 */
export function updateApp(data: PayAppApi.App) {
  return requestClient.put('/pay/app/update', data);
}

/** 修改支付应用状态 */
export function updateAppStatus(data: PayAppApi.AppUpdateStatusReqVO) {
  return requestClient.put('/pay/app/update-status', data);
}

/** 删除支付应用 */
export function deleteApp(id: number) {
  return requestClient.delete(`/pay/app/delete?id=${id}`);
}

/** 获取支付应用列表 */
export function getAppList() {
  return requestClient.get<PayAppApi.App[]>('/pay/app/list');
}
