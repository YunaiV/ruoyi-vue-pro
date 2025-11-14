import { requestClient } from '#/api/request';

export namespace MallPayStatisticsApi {
  /** 支付统计 */
  export interface PaySummaryResp {
    /** 充值金额，单位分 */
    rechargePrice: number;
  }
}

/** 获取钱包充值金额 */
export function getWalletRechargePrice() {
  return requestClient.get<MallPayStatisticsApi.PaySummaryResp>(
    '/statistics/pay/summary',
  );
}
