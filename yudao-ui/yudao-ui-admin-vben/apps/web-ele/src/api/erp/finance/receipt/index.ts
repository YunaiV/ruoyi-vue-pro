import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpFinanceReceiptApi {
  /** 收款单项 */
  export interface FinanceReceiptItem {
    id?: number;
    row_id?: number; // 前端使用的临时 ID
    bizId: number; // 业务ID
    bizType: number; // 业务类型
    bizNo: string; // 业务编号
    totalPrice: number; // 应收金额
    receiptedPrice: number; // 已收金额
    receiptPrice: number; // 本次收款
    remark?: string; // 备注
  }

  /** 收款单信息 */
  export interface FinanceReceipt {
    id?: number; // 收款单编号
    no: string; // 收款单号
    customerId: number; // 客户编号
    customerName?: string; // 客户名称
    receiptTime: Date; // 收款时间
    totalPrice: number; // 合计金额，单位：元
    discountPrice: number; // 优惠金额
    receiptPrice: number; // 实际收款金额
    status: number; // 状态
    remark: string; // 备注
    fileUrl?: string; // 附件
    accountId?: number; // 收款账户
    accountName?: string; // 账户名称
    financeUserId?: number; // 财务人员
    financeUserName?: string; // 财务人员姓名
    creator?: string; // 创建人
    creatorName?: string; // 创建人姓名
    items?: FinanceReceiptItem[]; // 收款明细
    bizNo?: string; // 业务单号
  }
}

/** 查询收款单分页 */
export function getFinanceReceiptPage(params: PageParam) {
  return requestClient.get<PageResult<ErpFinanceReceiptApi.FinanceReceipt>>(
    '/erp/finance-receipt/page',
    {
      params,
    },
  );
}

/** 查询收款单详情 */
export function getFinanceReceipt(id: number) {
  return requestClient.get<ErpFinanceReceiptApi.FinanceReceipt>(
    `/erp/finance-receipt/get?id=${id}`,
  );
}

/** 新增收款单 */
export function createFinanceReceipt(
  data: ErpFinanceReceiptApi.FinanceReceipt,
) {
  return requestClient.post('/erp/finance-receipt/create', data);
}

/** 修改收款单 */
export function updateFinanceReceipt(
  data: ErpFinanceReceiptApi.FinanceReceipt,
) {
  return requestClient.put('/erp/finance-receipt/update', data);
}

/** 更新收款单的状态 */
export function updateFinanceReceiptStatus(id: number, status: number) {
  return requestClient.put('/erp/finance-receipt/update-status', null, {
    params: { id, status },
  });
}

/** 删除收款单 */
export function deleteFinanceReceipt(ids: number[]) {
  return requestClient.delete('/erp/finance-receipt/delete', {
    params: {
      ids: ids.join(','),
    },
  });
}

/** 导出收款单 Excel */
export function exportFinanceReceipt(params: any) {
  return requestClient.download('/erp/finance-receipt/export-excel', {
    params,
  });
}
