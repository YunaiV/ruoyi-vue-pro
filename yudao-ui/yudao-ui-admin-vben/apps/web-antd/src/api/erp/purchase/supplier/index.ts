import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpSupplierApi {
  /** ERP 供应商信息 */
  export interface Supplier {
    id?: number; // 供应商编号
    name: string; // 供应商名称
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

  /** 供应商分页查询参数 */
  export interface SupplierPageParam extends PageParam {
    name?: string;
    mobile?: string;
    status?: number;
  }
}

/** 查询供应商分页 */
export function getSupplierPage(params: ErpSupplierApi.SupplierPageParam) {
  return requestClient.get<PageResult<ErpSupplierApi.Supplier>>(
    '/erp/supplier/page',
    { params },
  );
}

/** 获得供应商精简列表 */
export function getSupplierSimpleList() {
  return requestClient.get<ErpSupplierApi.Supplier[]>(
    '/erp/supplier/simple-list',
  );
}

/** 查询供应商详情 */
export function getSupplier(id: number) {
  return requestClient.get<ErpSupplierApi.Supplier>(
    `/erp/supplier/get?id=${id}`,
  );
}

/** 新增供应商 */
export function createSupplier(data: ErpSupplierApi.Supplier) {
  return requestClient.post('/erp/supplier/create', data);
}

/** 修改供应商 */
export function updateSupplier(data: ErpSupplierApi.Supplier) {
  return requestClient.put('/erp/supplier/update', data);
}

/** 删除供应商 */
export function deleteSupplier(id: number) {
  return requestClient.delete(`/erp/supplier/delete?id=${id}`);
}

/** 导出供应商 Excel */
export function exportSupplier(params: any) {
  return requestClient.download('/erp/supplier/export-excel', { params });
}
