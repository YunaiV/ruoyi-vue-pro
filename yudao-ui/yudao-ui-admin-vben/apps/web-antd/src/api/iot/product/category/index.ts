import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace IotProductCategoryApi {
  /** 产品分类 */
  export interface ProductCategory {
    id?: number; // 分类 ID
    name: string; // 分类名称
    parentId?: number; // 父级分类 ID
    sort?: number; // 分类排序
    status?: number; // 分类状态
    description?: string; // 分类描述
    createTime?: string; // 创建时间
  }
}

/** 查询产品分类分页 */
export function getProductCategoryPage(params: PageParam) {
  return requestClient.get<PageResult<IotProductCategoryApi.ProductCategory>>(
    '/iot/product-category/page',
    { params },
  );
}

/** 查询产品分类详情 */
export function getProductCategory(id: number) {
  return requestClient.get<IotProductCategoryApi.ProductCategory>(
    `/iot/product-category/get?id=${id}`,
  );
}

/** 新增产品分类 */
export function createProductCategory(
  data: IotProductCategoryApi.ProductCategory,
) {
  return requestClient.post('/iot/product-category/create', data);
}

/** 修改产品分类 */
export function updateProductCategory(
  data: IotProductCategoryApi.ProductCategory,
) {
  return requestClient.put('/iot/product-category/update', data);
}

/** 刪除产品分类 */
export function deleteProductCategory(id: number) {
  return requestClient.delete(`/iot/product-category/delete?id=${id}`);
}

/** 获取产品分类精简列表 */
export function getSimpleProductCategoryList() {
  return requestClient.get<IotProductCategoryApi.ProductCategory[]>(
    '/iot/product-category/simple-list',
  );
}
