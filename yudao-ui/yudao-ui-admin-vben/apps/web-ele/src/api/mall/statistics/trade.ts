import type { DataComparisonRespVO } from './common';

import { formatDateTime } from '@vben/utils';

import { requestClient } from '#/api/request';

export namespace MallTradeStatisticsApi {
  /** 交易状况 Request */
  export interface TradeTrendReqVO {
    times: [Date, Date];
  }

  /** 交易统计 Response */
  export interface TradeSummaryRespVO {
    yesterdayOrderCount: number;
    monthOrderCount: number;
    yesterdayPayPrice: number;
    monthPayPrice: number;
  }

  /** 交易状况统计 Response */
  export interface TradeTrendSummaryRespVO {
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
  export interface TradeOrderCountRespVO {
    undelivered?: number; // 待发货
    pickUp?: number; // 待核销
    afterSaleApply?: number; // 退款中
    auditingWithdraw?: number; // 提现待审核
  }

  /** 交易订单统计 Response */
  export interface TradeOrderSummaryRespVO {
    orderPayCount?: number; // 支付订单商品数
    orderPayPrice?: number; // 总支付金额，单位：分
  }

  /** 订单量趋势统计 Response */
  export interface TradeOrderTrendRespVO {
    date: string; // 日期
    orderPayCount: number; // 订单数量
    orderPayPrice: number; // 订单支付金额
  }
}

/** 查询交易统计 */
export function getTradeStatisticsSummary() {
  return requestClient.get<
    DataComparisonRespVO<MallTradeStatisticsApi.TradeSummaryRespVO>
  >('/statistics/trade/summary');
}

/** 获得交易状况统计 */
export function getTradeStatisticsAnalyse(
  params: MallTradeStatisticsApi.TradeTrendReqVO,
) {
  return requestClient.get<
    DataComparisonRespVO<MallTradeStatisticsApi.TradeTrendSummaryRespVO>
  >('/statistics/trade/analyse', { params });
}

/** 获得交易状况明细 */
export function getTradeStatisticsList(params: any) {
  return requestClient.get<MallTradeStatisticsApi.TradeTrendSummaryRespVO[]>(
    '/statistics/trade/list',
    { params },
  );
}

/** 导出交易状况明细 */
export function exportTradeStatisticsExcel(params: any) {
  return requestClient.download('/statistics/trade/export-excel', { params });
}

/** 获得交易订单数量 */
export function getOrderCount() {
  return requestClient.get<MallTradeStatisticsApi.TradeOrderCountRespVO>(
    '/statistics/trade/order-count',
  );
}

/** 获得交易订单数量对照 */
export function getOrderComparison() {
  return requestClient.get<
    DataComparisonRespVO<MallTradeStatisticsApi.TradeOrderSummaryRespVO>
  >('/statistics/trade/order-comparison');
}

/** 获得订单量趋势统计 */
export function getOrderCountTrendComparison(
  type: number,
  beginTime: Date,
  endTime: Date,
) {
  return requestClient.get<
    DataComparisonRespVO<MallTradeStatisticsApi.TradeOrderTrendRespVO>[]
  >('/statistics/trade/order-count-trend', {
    params: {
      type,
      beginTime: formatDateTime(beginTime),
      endTime: formatDateTime(endTime),
    },
  });
}
