import { requestClient } from '#/api/request';

export namespace MallCategoryApi {
  /** 产品分类 */
  export interface Category {
    /** 分类编号 */
    id?: number;
    /** 父分类编号 */
    parentId?: number;
    /** 分类名称 */
    name: string;
    /** 移动端分类图 */
    picUrl: string;
    /** 分类排序 */
    sort: number;
    /** 开启状态 */
    status: number;
  }
}

/** 创建商品分类 */
export function createCategory(data: MallCategoryApi.Category) {
  return requestClient.post('/product/category/create', data);
}

/** 更新商品分类 */
export function updateCategory(data: MallCategoryApi.Category) {
  return requestClient.put('/product/category/update', data);
}

/** 删除商品分类 */
export function deleteCategory(id: number) {
  return requestClient.delete(`/product/category/delete?id=${id}`);
}

/** 获得商品分类 */
export function getCategory(id: number) {
  return requestClient.get<MallCategoryApi.Category>(
    `/product/category/get?id=${id}`,
  );
}

/** 获得商品分类列表 */
export function getCategoryList(params: any) {
  return requestClient.get<MallCategoryApi.Category[]>(
    '/product/category/list',
    {
      params,
    },
  );
}

// 获得商品分类列表
export function getCategorySimpleList() {
  return requestClient.get<MallCategoryApi.Category[]>(
    '/product/category/list',
  );
}
