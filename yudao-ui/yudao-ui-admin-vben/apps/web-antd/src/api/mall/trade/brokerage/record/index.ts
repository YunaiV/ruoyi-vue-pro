import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallBrokerageRecordApi {
  /** 佣金记录 */
  export interface BrokerageRecord {
    id: number; // 编号
    userId: number; // 用户编号
    userNickname: string; // 用户昵称
    userAvatar: string; // 用户头像
    price: number; // 佣金金额，单位：分
    type: number; // 佣金类型
    orderId: number; // 关联订单编号
    orderNo: string; // 关联订单号
    createTime: Date; // 创建时间
    status: number; // 状态
    settlementTime: Date; // 结算时间
  }
}

/** 查询佣金记录列表 */
export function getBrokerageRecordPage(params: PageParam) {
  return requestClient.get<PageResult<MallBrokerageRecordApi.BrokerageRecord>>(
    '/trade/brokerage-record/page',
    { params },
  );
}

/** 查询佣金记录详情 */
export function getBrokerageRecord(id: number) {
  return requestClient.get<MallBrokerageRecordApi.BrokerageRecord>(
    `/trade/brokerage-record/get?id=${id}`,
  );
}
