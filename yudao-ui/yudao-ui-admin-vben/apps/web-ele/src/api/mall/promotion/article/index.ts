import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallArticleApi {
  /** 文章管理 */
  export interface Article {
    id: number; // 文章编号
    categoryId: number; // 分类编号
    title: string; // 文章标题
    author: string; // 作者
    picUrl: string; // 封面图
    introduction: string; // 文章简介
    browseCount: string; // 浏览数量
    sort: number; // 排序
    status: number; // 状态
    spuId: number; // 商品编号
    recommendHot: boolean; // 是否热门
    recommendBanner: boolean; // 是否轮播图
    content: string; // 文章内容
  }
}

/** 查询文章管理列表 */
export function getArticlePage(params: PageParam) {
  return requestClient.get<PageResult<MallArticleApi.Article>>(
    '/promotion/article/page',
    { params },
  );
}

/** 查询文章管理详情 */
export function getArticle(id: number) {
  return requestClient.get<MallArticleApi.Article>(
    `/promotion/article/get?id=${id}`,
  );
}

/** 新增文章管理 */
export function createArticle(data: MallArticleApi.Article) {
  return requestClient.post('/promotion/article/create', data);
}

/** 修改文章管理 */
export function updateArticle(data: MallArticleApi.Article) {
  return requestClient.put('/promotion/article/update', data);
}

/** 删除文章管理 */
export function deleteArticle(id: number) {
  return requestClient.delete(`/promotion/article/delete?id=${id}`);
}
