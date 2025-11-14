import type { PageParam, PageResult } from '@vben/request';

import type { MallDataComparisonResp } from './common';

import { requestClient } from '#/api/request';

export namespace MallProductStatisticsApi {
  /** 商品统计数据 */
  export interface ProductStatistics {
    /** 编号 */
    id: number;
    /** 统计日期 */
    day: string;
    /** 商品 SPU 编号 */
    spuId: number;
    /** 商品 SPU 名称 */
    spuName: string;
    /** 商品 SPU 图片 */
    spuPicUrl: string;
    /** 浏览次数 */
    browseCount: number;
    /** 浏览人数 */
    browseUserCount: number;
    /** 收藏次数 */
    favoriteCount: number;
    /** 加购次数 */
    cartCount: number;
    /** 下单次数 */
    orderCount: number;
    /** 支付次数 */
    orderPayCount: number;
    /** 支付金额 */
    orderPayPrice: number;
    /** 售后次数 */
    afterSaleCount: number;
    /** 退款金额 */
    afterSaleRefundPrice: number;
    /** 浏览转化率 */
    browseConvertPercent: number;
  }
}

/** 获得商品统计分析 */
export function getProductStatisticsAnalyse(params: PageParam) {
  return requestClient.get<
    MallDataComparisonResp<MallProductStatisticsApi.ProductStatistics>
  >('/statistics/product/analyse', { params });
}

/** 获得商品状况明细 */
export function getProductStatisticsList(params: PageParam) {
  return requestClient.get<MallProductStatisticsApi.ProductStatistics[]>(
    '/statistics/product/list',
    { params },
  );
}

/** 导出获得商品状况明细 Excel */
export function exportProductStatisticsExcel(params: PageParam) {
  return requestClient.download('/statistics/product/export-excel', { params });
}

/** 获得商品排行榜分页 */
export function getProductStatisticsRankPage(params: PageParam) {
  return requestClient.get<
    PageResult<MallProductStatisticsApi.ProductStatistics>
  >('/statistics/product/rank-page', { params });
}
