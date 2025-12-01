import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpFinancePaymentApi {
  /** 付款单信息 */
  export interface FinancePayment {
    id?: number; // 付款单编号
    no: string; // 付款单号
    supplierId?: number; // 供应商编号
    supplierName?: string; // 供应商名称
    paymentTime?: Date; // 付款时间
    totalPrice: number; // 合计金额，单位：元
    discountPrice: number; // 优惠金额
    paymentPrice: number; // 实际付款金额
    status: number; // 状态
    remark: string; // 备注
    fileUrl?: string; // 附件
    accountId?: number; // 付款账户
    accountName?: string; // 账户名称
    financeUserId?: number; // 财务人员
    financeUserName?: string; // 财务人员姓名
    creator?: string; // 创建人
    creatorName?: string; // 创建人姓名
    items?: FinancePaymentItem[]; // 付款明细
    bizNo?: string; // 业务单号
  }

  /** 付款单项 */
  export interface FinancePaymentItem {
    id?: number;
    row_id?: number; // 前端使用的临时 ID
    bizId: number; // 业务ID
    bizType: number; // 业务类型
    bizNo: string; // 业务编号
    totalPrice: number; // 应付金额
    paidPrice: number; // 已付金额
    paymentPrice: number; // 本次付款
    remark?: string; // 备注
  }
}

/** 查询付款单分页 */
export function getFinancePaymentPage(params: PageParam) {
  return requestClient.get<PageResult<ErpFinancePaymentApi.FinancePayment>>(
    '/erp/finance-payment/page',
    {
      params,
    },
  );
}

/** 查询付款单详情 */
export function getFinancePayment(id: number) {
  return requestClient.get<ErpFinancePaymentApi.FinancePayment>(
    `/erp/finance-payment/get?id=${id}`,
  );
}

/** 新增付款单 */
export function createFinancePayment(
  data: ErpFinancePaymentApi.FinancePayment,
) {
  return requestClient.post('/erp/finance-payment/create', data);
}

/** 修改付款单 */
export function updateFinancePayment(
  data: ErpFinancePaymentApi.FinancePayment,
) {
  return requestClient.put('/erp/finance-payment/update', data);
}

/** 更新付款单的状态 */
export function updateFinancePaymentStatus(id: number, status: number) {
  return requestClient.put('/erp/finance-payment/update-status', null, {
    params: { id, status },
  });
}

/** 删除付款单 */
export function deleteFinancePayment(ids: number[]) {
  return requestClient.delete('/erp/finance-payment/delete', {
    params: {
      ids: ids.join(','),
    },
  });
}

/** 导出付款单 Excel */
export function exportFinancePayment(params: any) {
  return requestClient.download('/erp/finance-payment/export-excel', {
    params,
  });
}
