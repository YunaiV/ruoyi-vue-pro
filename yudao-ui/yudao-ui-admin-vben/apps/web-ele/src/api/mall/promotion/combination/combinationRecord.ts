import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallCombinationRecordApi {
  /** 拼团记录 */
  export interface CombinationRecord {
    id: number; // 拼团记录编号
    activityId: number; // 拼团活动编号
    nickname: string; // 用户昵称
    avatar: string; // 用户头像
    headId: number; // 团长编号
    expireTime: string; // 过期时间
    userSize: number; // 可参团人数
    userCount: number; // 已参团人数
    status: number; // 拼团状态
    spuName: string; // 商品名字
    picUrl: string; // 商品图片
    virtualGroup: boolean; // 是否虚拟成团
    startTime: string; // 开始时间 (订单付款后开始的时间)
    endTime: string; // 结束时间（成团时间/失败时间）
  }

  /** 拼团记录概要信息 */
  export interface RecordSummary {
    pendingCount: number; // 待成团数量
    successCount: number; // 已成团数量
    failCount: number; // 已失败数量
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
