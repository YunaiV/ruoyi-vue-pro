import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallBrandApi {
  /** 商品品牌 */
  export interface Brand {
    /** 品牌编号 */
    id?: number;
    /** 品牌名称 */
    name: string;
    /** 品牌图片 */
    picUrl: string;
    /** 品牌排序 */
    sort?: number;
    /** 品牌描述 */
    description?: string;
    /** 开启状态 */
    status: number;
  }
}

/** 创建商品品牌 */
export function createBrand(data: MallBrandApi.Brand) {
  return requestClient.post('/product/brand/create', data);
}

/** 更新商品品牌 */
export function updateBrand(data: MallBrandApi.Brand) {
  return requestClient.put('/product/brand/update', data);
}

/** 删除商品品牌 */
export function deleteBrand(id: number) {
  return requestClient.delete(`/product/brand/delete?id=${id}`);
}

/** 获得商品品牌 */
export function getBrand(id: number) {
  return requestClient.get<MallBrandApi.Brand>(`/product/brand/get?id=${id}`);
}

/** 获得商品品牌列表 */
export function getBrandPage(params: PageParam) {
  return requestClient.get<PageResult<MallBrandApi.Brand>>(
    '/product/brand/page',
    {
      params,
    },
  );
}

/** 获得商品品牌精简信息列表 */
export function getSimpleBrandList() {
  return requestClient.get<MallBrandApi.Brand[]>(
    '/product/brand/list-all-simple',
  );
}
