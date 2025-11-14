import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

/** 素材类型枚举 */
export enum MaterialType {
  IMAGE = 1, // 图片
  THUMB = 4, // 缩略图
  VIDEO = 3, // 视频
  VOICE = 2, // 语音
}

export namespace MpMaterialApi {
  /** 素材信息 */
  export interface Material {
    id?: number;
    accountId: number;
    type: MaterialType;
    mediaId: string;
    url: string;
    name: string;
    size: number;
    remark?: string;
    createTime?: Date;
  }
}

/** 查询素材列表 */
export function getMaterialPage(params: PageParam) {
  return requestClient.get<PageResult<MpMaterialApi.Material>>(
    '/mp/material/page',
    {
      params,
    },
  );
}

/** 删除永久素材 */
export function deletePermanentMaterial(id: number) {
  return requestClient.delete('/mp/material/delete-permanent', {
    params: { id },
  });
}
