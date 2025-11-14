import type { MallDataComparisonResp } from './common';

import { formatDate2 } from '@vben/utils';

import { requestClient } from '#/api/request';

export namespace MallTradeStatisticsApi {
  /** 交易统计 Response */
  export interface TradeSummary {
    yesterdayOrderCount: number;
    monthOrderCount: number;
    yesterdayPayPrice: number;
    monthPayPrice: number;
  }

  /** 交易状况 Request */
  export interface TradeTrendReq {
    times: Date[];
  }

  /** 交易状况统计 Response */
  export interface TradeTrendSummary {
    time: string;
    turnoverPrice: number;
    orderPayPrice: number;
    rechargePrice: number;
    expensePrice: number;
    walletPayPrice: number;
    brokerageSettlementPrice: number;
    afterSaleRefundPrice: number;
  }

  /** 交易订单数量 Response */
  export interface TradeOrderCount {
    /** 待发货 */
    undelivered?: number;
    /** 待核销 */
    pickUp?: number;
    /** 退款中 */
    afterSaleApply?: number;
    /** 提现待审核 */
    auditingWithdraw?: number;
  }

  /** 交易订单统计 Response */
  export interface TradeOrderSummary {
    /** 支付订单商品数 */
    orderPayCount?: number;
    /** 总支付金额，单位：分 */
    orderPayPrice?: number;
  }

  /** 订单量趋势统计 Response */
  export interface TradeOrderTrend {
    /** 日期 */
    date: string;
    /** 订单数量 */
    orderPayCount: number;
    /** 订单支付金额 */
    orderPayPrice: number;
  }
}

/** 时间参数需要格式化, 确保接口能识别 */
const formatDateParam = (params: MallTradeStatisticsApi.TradeTrendReq) => {
  return {
    times: [
      formatDate2(params.times[0] || new Date()),
      formatDate2(params.times[1] || new Date()),
    ],
  };
};

/** 查询交易统计 */
export function getTradeStatisticsSummary() {
  return requestClient.get<
    MallDataComparisonResp<MallTradeStatisticsApi.TradeSummary>
  >('/statistics/trade/summary');
}

/** 获得交易状况统计 */
export function getTradeStatisticsAnalyse(
  params: MallTradeStatisticsApi.TradeTrendReq,
) {
  return requestClient.get<
    MallDataComparisonResp<MallTradeStatisticsApi.TradeTrendSummary>
  >('/statistics/trade/analyse', { params: formatDateParam(params) });
}

/** 获得交易状况明细 */
export function getTradeStatisticsList(
  params: MallTradeStatisticsApi.TradeTrendReq,
) {
  return requestClient.get<MallTradeStatisticsApi.TradeTrendSummary[]>(
    '/statistics/trade/list',
    { params: formatDateParam(params) },
  );
}

/** 导出交易状况明细 */
export function exportTradeStatisticsExcel(
  params: MallTradeStatisticsApi.TradeTrendReq,
) {
  return requestClient.download('/statistics/trade/export-excel', {
    params: formatDateParam(params),
  });
}

/** 获得交易订单数量 */
export function getOrderCount() {
  return requestClient.get<MallTradeStatisticsApi.TradeOrderCount>(
    '/statistics/trade/order-count',
  );
}

/** 获得交易订单数量对照 */
export function getOrderComparison() {
  return requestClient.get<
    MallDataComparisonResp<MallTradeStatisticsApi.TradeOrderSummary>
  >('/statistics/trade/order-comparison');
}

/** 获得订单量趋势统计 */
export function getOrderCountTrendComparison(
  type: number,
  beginTime: Date,
  endTime: Date,
) {
  return requestClient.get<
    MallDataComparisonResp<MallTradeStatisticsApi.TradeOrderTrend>[]
  >('/statistics/trade/order-count-trend', {
    params: {
      type,
      beginTime: formatDate2(beginTime),
      endTime: formatDate2(endTime),
    },
  });
}
