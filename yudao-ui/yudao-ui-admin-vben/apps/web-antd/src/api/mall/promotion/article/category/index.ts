import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallArticleCategoryApi {
  /** 文章分类 */
  export interface ArticleCategory {
    id: number; // 分类编号
    name: string; // 分类名称
    picUrl: string; // 分类图片
    status: number; // 状态
    sort: number; // 排序
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
