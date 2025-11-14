import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace PayWalletApi {
  /** 用户钱包查询参数 */
  export interface PayWalletUserReq {
    userId: number;
  }

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

  /** 钱包分页请求 */
  export interface WalletPageReqVO extends PageParam {
    userId?: number;
    userType?: number;
    balance?: number;
    totalExpense?: number;
    totalRecharge?: number;
    freezePrice?: number;
  }

  /** 钱包修改余额 */
  export interface PayWalletUpdateBalanceReqVO {
    userId: number;
    balance: number;
  }
}

/** 查询用户钱包详情 */
export function getWallet(params: PayWalletApi.PayWalletUserReq) {
  return requestClient.get<PayWalletApi.Wallet>('/pay/wallet/get', {
    params,
  });
}

/** 查询会员钱包列表 */
export function getWalletPage(params: PayWalletApi.WalletPageReqVO) {
  return requestClient.get<PageResult<PayWalletApi.Wallet>>(
    '/pay/wallet/page',
    {
      params,
    },
  );
}

/** 修改会员钱包余额 */
export function updateWalletBalance(
  data: PayWalletApi.PayWalletUpdateBalanceReqVO,
) {
  return requestClient.put('/pay/wallet/update-balance', data);
}
