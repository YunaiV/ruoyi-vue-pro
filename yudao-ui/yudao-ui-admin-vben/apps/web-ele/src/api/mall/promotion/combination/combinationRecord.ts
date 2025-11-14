import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallCombinationRecordApi {
  /** 拼团记录 */
  export interface CombinationRecord {
    /** 拼团记录编号 */
    id: number;
    /** 拼团活动编号 */
    activityId: number;
    /** 用户昵称 */
    nickname: string;
    /** 用户头像 */
    avatar: string;
    /** 团长编号 */
    headId: number;
    /** 过期时间 */
    expireTime: string;
    /** 可参团人数 */
    userSize: number;
    /** 已参团人数 */
    userCount: number;
    /** 拼团状态 */
    status: number;
    /** 商品名字 */
    spuName: string;
    /** 商品图片 */
    picUrl: string;
    /** 是否虚拟成团 */
    virtualGroup: boolean;
    /** 开始时间 (订单付款后开始的时间) */
    startTime: string;
    /** 结束时间（成团时间/失败时间） */
    endTime: string;
  }

  /** 拼团记录概要信息 */
  export interface RecordSummary {
    /** 待成团数量 */
    pendingCount: number;
    /** 已成团数量 */
    successCount: number;
    /** 已失败数量 */
    failCount: number;
  }
}

/** 查询拼团记录列表 */
export function getCombinationRecordPage(params: PageParam) {
  return requestClient.get<
    PageResult<MallCombinationRecordApi.CombinationRecord>
  >('/promotion/combination-record/page', { params });
}

/** 获得拼团记录的概要信息 */
export function getCombinationRecordSummary() {
  return requestClient.get<MallCombinationRecordApi.RecordSummary>(
    '/promotion/combination-record/get-summary',
  );
}
