import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace IotProductCategoryApi {
  /** IoT 產品分類 VO */
  export interface ProductCategory {
    id?: number; // 分類 ID
    name: string; // 分類名稱
    parentId?: number; // 父级分類 ID
    sort?: number; // 分類排序
    status?: number; // 分類狀態
    description?: string; // 分類描述
    createTime?: string; // 創建時間
  }
}

/** 查詢產品分類分頁 */
export function getProductCategoryPage(params: PageParam) {
  return requestClient.get<PageResult<IotProductCategoryApi.ProductCategory>>(
    '/iot/product-category/page',
    { params },
  );
}

/** 查詢產品分類詳情 */
export function getProductCategory(id: number) {
  return requestClient.get<IotProductCategoryApi.ProductCategory>(
    `/iot/product-category/get?id=${id}`,
  );
}

/** 新增產品分類 */
export function createProductCategory(
  data: IotProductCategoryApi.ProductCategory,
) {
  return requestClient.post('/iot/product-category/create', data);
}

/** 修改產品分類 */
export function updateProductCategory(
  data: IotProductCategoryApi.ProductCategory,
) {
  return requestClient.put('/iot/product-category/update', data);
}

/** 刪除產品分類 */
export function deleteProductCategory(id: number) {
  return requestClient.delete(`/iot/product-category/delete?id=${id}`);
}

/** 獲取產品分類精簡列表 */
export function getSimpleProductCategoryList() {
  return requestClient.get<IotProductCategoryApi.ProductCategory[]>(
    '/iot/product-category/simple-list',
  );
}
