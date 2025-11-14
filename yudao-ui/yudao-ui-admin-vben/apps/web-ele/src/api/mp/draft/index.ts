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
}

/** 查询草稿列表 */
export function getDraftPage(params: PageParam) {
  return requestClient.get<PageResult<MpDraftApi.Draft>>('/mp/draft/page', {
    params,
  });
}

/** 创建草稿 */
export function createDraft(accountId: number, articles: MpDraftApi.Article[]) {
  return requestClient.post('/mp/draft/create', articles, {
    params: { accountId },
  });
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
