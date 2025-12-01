import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MpDraftApi {
  /** 草稿文章信息 */
  export interface Article {
    title: string;
    author: string;
    digest: string;
    content: string;
    contentSourceUrl: string;
    thumbMediaId: string;
    showCoverPic: number;
    needOpenComment: number;
    onlyFansCanComment: number;
  }

  /** 草稿信息 */
  export interface Draft {
    id?: number;
    accountId: number;
    mediaId: string;
    articles: Article[];
    createTime?: Date;
  }

  /** 图文项（包含预览字段） */
  export interface NewsItem {
    title: string;
    thumbMediaId: string;
    author: string;
    digest: string;
    showCoverPic: number;
    content: string;
    contentSourceUrl: string;
    needOpenComment: number;
    onlyFansCanComment: number;
    thumbUrl: string;
    picUrl?: string; // 用于预览封面
  }

  /** 图文列表 */
  export interface NewsItemList {
    newsItem: NewsItem[];
  }

  /** 草稿文章（用于展示） */
  export interface DraftArticle {
    mediaId: string;
    content: NewsItemList;
    updateTime: number;
  }
}

/** 创建空的图文项 */
export function createEmptyNewsItem(): MpDraftApi.NewsItem {
  return {
    title: '',
    thumbMediaId: '',
    author: '',
    digest: '',
    showCoverPic: 0,
    content: '',
    contentSourceUrl: '',
    needOpenComment: 0,
    onlyFansCanComment: 0,
    thumbUrl: '',
  };
}

/** 查询草稿列表 */
export function getDraftPage(params: PageParam) {
  return requestClient.get<PageResult<MpDraftApi.Draft>>('/mp/draft/page', {
    params,
  });
}

/** 创建草稿 */
export function createDraft(accountId: number, articles: MpDraftApi.Article[]) {
  return requestClient.post(
    '/mp/draft/create',
    { articles },
    {
      params: { accountId },
    },
  );
}

/** 更新草稿 */
export function updateDraft(
  accountId: number,
  mediaId: string,
  articles: MpDraftApi.Article[],
) {
  return requestClient.put('/mp/draft/update', articles, {
    params: { accountId, mediaId },
  });
}

/** 删除草稿 */
export function deleteDraft(accountId: number, mediaId: string) {
  return requestClient.delete('/mp/draft/delete', {
    params: { accountId, mediaId },
  });
}
