import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ErpProductApi {
  /** ERP 产品信息 */
  export interface Product {
    id?: number; // 产品编号
    name: string; // 产品名称
    barCode: string; // 产品条码
    categoryId: number; // 产品类型编号
    unitId: number; // 单位编号
    unitName?: string; // 单位名字
    status: number; // 产品状态
    standard: string; // 产品规格
    remark: string; // 产品备注
    expiryDay: number; // 保质期天数
    weight: number; // 重量（kg）
    purchasePrice: number; // 采购价格，单位：元
    salePrice: number; // 销售价格，单位：元
    minPrice: number; // 最低价格，单位：元
  }
}

/** 查询产品分页 */
export function getProductPage(params: PageParam) {
  return requestClient.get<PageResult<ErpProductApi.Product>>(
    '/erp/product/page',
    { params },
  );
}

/** 查询产品精简列表 */
export function getProductSimpleList() {
  return requestClient.get<ErpProductApi.Product[]>('/erp/product/simple-list');
}

/** 查询产品详情 */
export function getProduct(id: number) {
  return requestClient.get<ErpProductApi.Product>(`/erp/product/get?id=${id}`);
}

/** 新增产品 */
export function createProduct(data: ErpProductApi.Product) {
  return requestClient.post('/erp/product/create', data);
}

/** 修改产品 */
export function updateProduct(data: ErpProductApi.Product) {
  return requestClient.put('/erp/product/update', data);
}

/** 删除产品 */
export function deleteProduct(id: number) {
  return requestClient.delete(`/erp/product/delete?id=${id}`);
}

/** 导出产品 Excel */
export function exportProduct(params: any) {
  return requestClient.download('/erp/product/export-excel', { params });
}
