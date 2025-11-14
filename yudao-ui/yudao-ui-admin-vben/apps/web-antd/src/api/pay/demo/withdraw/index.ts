import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace DemoWithdrawApi {
  /** 示例提现单信息 */
  export interface Withdraw {
    id?: number;
    subject: string;
    price: number;
    userName: string;
    userAccount: string;
    type: number;
    status?: number;
    payTransferId?: number;
    transferChannelCode?: string;
    transferTime?: Date;
    transferErrorMsg?: string;
  }
}

/** 查询示例提现单列表 */
export function getDemoWithdrawPage(params: PageParam) {
  return requestClient.get<PageResult<DemoWithdrawApi.Withdraw>>(
    '/pay/demo-withdraw/page',
    {
      params,
    },
  );
}

/** 创建示例提现单 */
export function createDemoWithdraw(data: DemoWithdrawApi.Withdraw) {
  return requestClient.post('/pay/demo-withdraw/create', data);
}

/** 发起提现单转账 */
export function transferDemoWithdraw(id: number) {
  return requestClient.post(`/pay/demo-withdraw/transfer?id=${id}`);
}
