import type { MallDataComparisonResp } from './common';

import { formatDate } from '@vben/utils';

import { requestClient } from '#/api/request';

export namespace MallMemberStatisticsApi {
  /** 会员分析 Request */
  export interface AnalyseReq {
    times: Date[]; // 时间范围
  }

  /** 会员分析对照数据 Response */
  export interface AnalyseComparison {
    registerUserCount: number; // 注册用户数
    visitUserCount: number; // 访问用户数
    rechargeUserCount: number; // 充值用户数
  }

  /** 会员分析 Response */
  export interface Analyse {
    visitUserCount: number; // 访问用户数
    orderUserCount: number; // 下单用户数
    payUserCount: number; // 支付用户数
    atv: number; // 平均客单价
    comparison: MallDataComparisonResp<AnalyseComparison>; // 对照数据
  }

  /** 会员地区统计 Response */
  export interface AreaStatistics {
    areaId: number; // 地区ID
    areaName: string; // 地区名称
    userCount: number; // 用户数
    orderCreateUserCount: number; // 下单用户数
    orderPayUserCount: number; // 支付用户数
    orderPayPrice: number; // 支付金额
  }

  /** 会员性别统计 Response */
  export interface SexStatistics {
    sex: number; // 性别
    userCount: number; // 用户数
  }

  /** 会员统计 Response */
  export interface Summary {
    userCount: number; // 用户数
    rechargeUserCount: number; // 充值用户数
    rechargePrice: number; // 充值金额
    expensePrice: number; // 消费金额
  }

  /** 会员终端统计 Response */
  export interface TerminalStatistics {
    terminal: number; // 终端
    userCount: number; // 用户数
  }

  /** 会员数量统计 Response */
  export interface Count {
    visitUserCount: string; // 用户访问量
    registerUserCount: number; // 注册用户数量
  }

  /** 会员注册数量 Response */
  export interface RegisterCount {
    date: string; // 日期
    count: number; // 数量
  }
}

/** 查询会员统计 */
export function getMemberSummary() {
  return requestClient.get<MallMemberStatisticsApi.Summary>(
    '/statistics/member/summary',
  );
}

/** 查询会员分析数据 */
export function getMemberAnalyse(params: MallMemberStatisticsApi.AnalyseReq) {
  return requestClient.get<MallMemberStatisticsApi.Analyse>(
    '/statistics/member/analyse',
    {
      params: {
        times: [formatDate(params.times[0]), formatDate(params.times[1])],
      },
    },
  );
}

/** 按照省份，查询会员统计列表 */
export function getMemberAreaStatisticsList() {
  return requestClient.get<MallMemberStatisticsApi.AreaStatistics[]>(
    '/statistics/member/area-statistics-list',
  );
}

/** 按照性别，查询会员统计列表 */
export function getMemberSexStatisticsList() {
  return requestClient.get<MallMemberStatisticsApi.SexStatistics[]>(
    '/statistics/member/sex-statistics-list',
  );
}

/** 按照终端，查询会员统计列表 */
export function getMemberTerminalStatisticsList() {
  return requestClient.get<MallMemberStatisticsApi.TerminalStatistics[]>(
    '/statistics/member/terminal-statistics-list',
  );
}

/** 获得用户数量量对照 */
export function getUserCountComparison() {
  return requestClient.get<
    MallDataComparisonResp<MallMemberStatisticsApi.Count>
  >('/statistics/member/user-count-comparison');
}

/** 获得会员注册数量列表 */
export function getMemberRegisterCountList(beginTime: Date, endTime: Date) {
  return requestClient.get<MallMemberStatisticsApi.RegisterCount[]>(
    '/statistics/member/register-count-list',
    {
      params: {
        times: [formatDate(beginTime), formatDate(endTime)],
      },
    },
  );
}
