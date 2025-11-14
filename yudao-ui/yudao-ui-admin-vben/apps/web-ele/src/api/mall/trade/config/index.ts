import { requestClient } from '#/api/request';

export namespace MallTradeConfigApi {
  /** 交易中心配置 */
  export interface Config {
    id?: number;
    afterSaleRefundReasons?: string[];
    afterSaleReturnReasons?: string[];
    deliveryExpressFreeEnabled?: boolean;
    deliveryExpressFreePrice?: number;
    deliveryPickUpEnabled?: boolean;
    brokerageEnabled?: boolean;
    brokerageEnabledCondition?: number;
    brokerageBindMode?: number;
    brokeragePosterUrls?: string;
    brokerageFirstPercent?: number;
    brokerageSecondPercent?: number;
    brokerageWithdrawMinPrice?: number;
    brokerageFrozenDays?: number;
    brokerageWithdrawTypes?: string;
    tencentLbsKey?: string;
  }
}

/** 查询交易中心配置详情 */
export function getTradeConfig() {
  return requestClient.get<MallTradeConfigApi.Config>('/trade/config/get');
}

/** 保存交易中心配置 */
export function saveTradeConfig(data: MallTradeConfigApi.Config) {
  return requestClient.put('/trade/config/save', data);
}
