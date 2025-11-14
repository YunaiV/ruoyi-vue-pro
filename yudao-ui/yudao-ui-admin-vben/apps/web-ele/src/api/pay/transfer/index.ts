import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace PayTransferApi {
  /** 转账单信息 */
  export interface Transfer {
    id: number;
    no: string;
    appId: number;
    appName: string;
    channelId: number;
    channelCode: string;
    merchantTransferId: string;
    channelTransferNo: string;
    price: number;
    subject: string;
    userName: string;
    userAccount: string;
    userIp: string;
    status: number;
    successTime: Date;
    createTime: Date;
    updateTime: Date;
    notifyUrl: string;
    channelNotifyData: string;
  }

  /** 转账单分页请求 */
  export interface TransferPageReqVO extends PageParam {
    no?: string;
    appId?: number;
    channelCode?: string;
    merchantOrderId?: string;
    status?: number;
    userName?: string;
    userAccount?: string;
    channelTransferNo?: string;
    createTime?: Date[];
  }
}

/** 查询转账单列表 */
export function getTransferPage(params: PayTransferApi.TransferPageReqVO) {
  return requestClient.get<PageResult<PayTransferApi.Transfer>>(
    '/pay/transfer/page',
    {
      params,
    },
  );
}

/** 查询转账单详情 */
export function getTransfer(id: number) {
  return requestClient.get<PayTransferApi.Transfer>(
    `/pay/transfer/get?id=${id}`,
  );
}

/** 导出转账单 */
export function exportTransfer(params: any) {
  return requestClient.download('/pay/transfer/export-excel', {
    params,
  });
}
