import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WalletTransactionApi {
  /** 钱包交易流水信息 */
  export interface Transaction {
    id: number;
    walletId: number;
    title: string;
    price: number;
    balance: number;
  }
}

/** 查询钱包交易流水列表 */
export function getTransactionPage(params: PageParam) {
  return requestClient.get<PageResult<WalletTransactionApi.Transaction>>(
    '/pay/wallet-transaction/page',
    {
      params,
    },
  );
}
