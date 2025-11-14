import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpCustomerApi {
  /** ERP 客户信息 */
  export interface Customer {
    id?: number; // 客户编号
    name: string; // 客户名称
    contact: string; // 联系人
    mobile: string; // 手机号码
    telephone: string; // 联系电话
    email: string; // 电子邮箱
    fax: string; // 传真
    remark: string; // 备注
    status: number; // 开启状态
    sort: number; // 排序
    taxNo: string; // 纳税人识别号
    taxPercent: number; // 税率
    bankName: string; // 开户行
    bankAccount: string; // 开户账号
    bankAddress: string; // 开户地址
  }

  /** 客户分页查询参数 */
  export interface CustomerPageParam extends PageParam {
    name?: string;
    mobile?: string;
    status?: number;
  }
}

/** 查询客户分页 */
export function getCustomerPage(params: ErpCustomerApi.CustomerPageParam) {
  return requestClient.get<PageResult<ErpCustomerApi.Customer>>(
    '/erp/customer/page',
    { params },
  );
}

/** 查询客户精简列表 */
export function getCustomerSimpleList() {
  return requestClient.get<ErpCustomerApi.Customer[]>(
    '/erp/customer/simple-list',
  );
}

/** 查询客户详情 */
export function getCustomer(id: number) {
  return requestClient.get<ErpCustomerApi.Customer>(
    `/erp/customer/get?id=${id}`,
  );
}

/** 新增客户 */
export function createCustomer(data: ErpCustomerApi.Customer) {
  return requestClient.post('/erp/customer/create', data);
}

/** 修改客户 */
export function updateCustomer(data: ErpCustomerApi.Customer) {
  return requestClient.put('/erp/customer/update', data);
}

/** 删除客户 */
export function deleteCustomer(id: number) {
  return requestClient.delete(`/erp/customer/delete?id=${id}`);
}

/** 导出客户 Excel */
export function exportCustomer(params: any) {
  return requestClient.download('/erp/customer/export-excel', { params });
}
