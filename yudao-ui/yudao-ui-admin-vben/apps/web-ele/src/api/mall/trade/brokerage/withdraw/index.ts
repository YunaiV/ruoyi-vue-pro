import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallBrokerageWithdrawApi {
  /** 佣金提现 */
  export interface BrokerageWithdraw {
    id: number; // 编号
    userId: number; // 用户编号
    price: number; // 提现金额，单位：分
    feePrice: number; // 手续费，单位：分
    totalPrice: number; // 总金额，单位：分
    type: number; // 提现类型
    userName: string; // 用户名称
    userAccount: string; // 用户账号
    bankName: string; // 银行名称
    bankAddress: string; // 银行地址
    qrCodeUrl: string; // 收款码地址
    status: number; // 状态
    auditReason: string; // 审核备注
    auditTime: Date; // 审核时间
    remark: string; // 备注
    payTransferId?: number; // 支付转账编号
    transferChannelCode?: string; // 转账渠道编码
    transferTime?: Date; // 转账时间
    transferErrorMsg?: string; // 转账错误信息
  }

  /** 驳回申请请求 */
  export interface BrokerageWithdrawRejectReqVO {
    id: number; // 编号
    auditReason: string; // 驳回原因
  }
}

/** 查询佣金提现列表 */
export function getBrokerageWithdrawPage(params: PageParam) {
  return requestClient.get<
    PageResult<MallBrokerageWithdrawApi.BrokerageWithdraw>
  >('/trade/brokerage-withdraw/page', { params });
}

/** 佣金提现 - 通过申请 */
export function approveBrokerageWithdraw(id: number) {
  return requestClient.put(`/trade/brokerage-withdraw/approve?id=${id}`);
}

/** 审核佣金提现 - 驳回申请 */
export function rejectBrokerageWithdraw(
  data: MallBrokerageWithdrawApi.BrokerageWithdrawRejectReqVO,
) {
  return requestClient.put('/trade/brokerage-withdraw/reject', data);
}
