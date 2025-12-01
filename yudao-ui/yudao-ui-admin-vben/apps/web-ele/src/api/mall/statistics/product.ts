import type { PageParam, PageResult } from '@vben/request';

import type { DataComparisonRespVO } from './common';

import { requestClient } from '#/api/request';

export namespace MallProductStatisticsApi {
  /** 商品统计数据 */
  export interface ProductStatisticsRespVO {
    id: number; // 编号
    day: string; // 统计日期
    spuId: number; // 商品 SPU 编号
    spuName: string; // 商品 SPU 名称
    spuPicUrl: string; // 商品 SPU 图片
    browseCount: number; // 浏览次数
    browseUserCount: number; // 浏览人数
    favoriteCount: number; // 收藏次数
    cartCount: number; // 加购次数
    orderCount: number; // 下单次数
    orderPayCount: number; // 支付次数
    orderPayPrice: number; // 支付金额
    afterSaleCount: number; // 售后次数
    afterSaleRefundPrice: number; // 退款金额
    browseConvertPercent: number; // 浏览转化率
  }
}

/** 获得商品统计分析 */
export function getProductStatisticsAnalyse(params: any) {
  return requestClient.get<
    DataComparisonRespVO<MallProductStatisticsApi.ProductStatisticsRespVO>
  >('/statistics/product/analyse', { params });
}

/** 获得商品状况明细 */
export function getProductStatisticsList(params: any) {
  return requestClient.get<MallProductStatisticsApi.ProductStatisticsRespVO[]>(
    '/statistics/product/list',
    { params },
  );
}

/** 导出获得商品状况明细 Excel */
export function exportProductStatisticsExcel(params: any) {
  return requestClient.download('/statistics/product/export-excel', { params });
}

/** 获得商品排行榜分页 */
export function getProductStatisticsRankPage(params: PageParam) {
  return requestClient.get<
    PageResult<MallProductStatisticsApi.ProductStatisticsRespVO>
  >('/statistics/product/rank-page', { params });
}
