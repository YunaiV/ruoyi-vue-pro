import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace PayWalletApi {
  /** 钱包信息 */
  export interface Wallet {
    id: number;
    userId: number;
    userType: number;
    balance: number;
    totalExpense: number;
    totalRecharge: number;
    freezePrice: number;
  }

  /** 钱包查询参数 */
  export interface WalletUserQueryReqVO {
    userId: number;
  }

  /** 钱包修改余额 */
  export interface WalletUpdateBalanceReqVO {
    userId: number;
    balance: number;
  }
}

/** 查询用户钱包详情 */
export function getWallet(params: PayWalletApi.WalletUserQueryReqVO) {
  return requestClient.get<PayWalletApi.Wallet>('/pay/wallet/get', {
    params,
  });
}

/** 查询会员钱包列表 */
export function getWalletPage(params: PageParam) {
  return requestClient.get<PageResult<PayWalletApi.Wallet>>(
    '/pay/wallet/page',
    {
      params,
    },
  );
}

/** 修改会员钱包余额 */
export function updateWalletBalance(
  data: PayWalletApi.WalletUpdateBalanceReqVO,
) {
  return requestClient.put('/pay/wallet/update-balance', data);
}
