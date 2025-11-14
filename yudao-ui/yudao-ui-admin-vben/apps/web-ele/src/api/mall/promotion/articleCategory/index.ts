import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallArticleCategoryApi {
  /** 文章分类 */
  export interface ArticleCategory {
    /** 分类编号 */
    id: number;
    /** 分类名称 */
    name: string;
    /** 分类图片 */
    picUrl: string;
    /** 状态 */
    status: number;
    /** 排序 */
    sort: number;
  }
}

/** 查询文章分类列表 */
export function getArticleCategoryPage(params: PageParam) {
  return requestClient.get<PageResult<MallArticleCategoryApi.ArticleCategory>>(
    '/promotion/article-category/page',
    { params },
  );
}

/** 查询文章分类精简信息列表 */
export function getSimpleArticleCategoryList() {
  return requestClient.get<MallArticleCategoryApi.ArticleCategory[]>(
    '/promotion/article-category/list-all-simple',
  );
}

/** 查询文章分类详情 */
export function getArticleCategory(id: number) {
  return requestClient.get<MallArticleCategoryApi.ArticleCategory>(
    `/promotion/article-category/get?id=${id}`,
  );
}

/** 新增文章分类 */
export function createArticleCategory(
  data: MallArticleCategoryApi.ArticleCategory,
) {
  return requestClient.post('/promotion/article-category/create', data);
}

/** 修改文章分类 */
export function updateArticleCategory(
  data: MallArticleCategoryApi.ArticleCategory,
) {
  return requestClient.put('/promotion/article-category/update', data);
}

/** 删除文章分类 */
export function deleteArticleCategory(id: number) {
  return requestClient.delete(`/promotion/article-category/delete?id=${id}`);
}
