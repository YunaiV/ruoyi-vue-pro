import type { Ref } from 'vue';

import type { ReplyType } from '@vben/constants';

import { unref } from 'vue';

export interface Reply {
  accountId: number;
  articles?: any[];
  content?: null | string;
  description?: null | string;
  hqMusicUrl?: null | string;
  introduction?: null | string;
  mediaId?: null | string;
  musicUrl?: null | string;
  name?: null | string;
  thumbMediaId?: null | string;
  thumbMediaUrl?: null | string;
  title?: null | string;
  type: ReplyType;
  url?: null | string;
}

/** 利用旧的 reply[accountId, type] 初始化新的 Reply */
export function createEmptyReply(old: Ref<Reply> | Reply): Reply {
  return {
    accountId: unref(old).accountId,
    articles: [],
    content: null,
    description: null,
    hqMusicUrl: null,
    introduction: null,
    mediaId: null,
    musicUrl: null,
    name: null,
    thumbMediaId: null,
    thumbMediaUrl: null,
    title: null,
    type: unref(old).type,
    url: null,
  };
}
