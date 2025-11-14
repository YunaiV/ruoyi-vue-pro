import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallCommentApi {
  export interface Property {
    propertyId: number;
    propertyName: string;
    valueId: number;
    valueName: string;
  }
  /** 商品评论 */
  export interface Comment {
    id: number;
    userId: number;
    userNickname: string;
    userAvatar: string;
    anonymous: boolean;
    orderId: number;
    orderItemId: number;
    spuId: number;
    spuName: string;
    skuId: number;
    visible: boolean;
    scores: number;
    descriptionScores: number;
    benefitScores: number;
    content: string;
    picUrls: string[];
    replyStatus: boolean;
    replyUserId: number;
    replyContent: string;
    replyTime: Date;
    createTime: Date;
    skuProperties: Property[];
  }

  /** 评论可见性更新 */
  export interface CommentVisibleUpdate {
    id: number;
    visible: boolean;
  }

  /** 评论回复 */
  export interface CommentReply {
    id: number;
    replyContent: string;
  }
}

/** 查询商品评论列表 */
export function getCommentPage(params: PageParam) {
  return requestClient.get<PageResult<MallCommentApi.Comment>>(
    '/product/comment/page',
    { params },
  );
}

/** 查询商品评论详情 */
export function getComment(id: number) {
  return requestClient.get<MallCommentApi.Comment>(
    `/product/comment/get?id=${id}`,
  );
}

/** 添加自评 */
export function createComment(data: MallCommentApi.Comment) {
  return requestClient.post('/product/comment/create', data);
}

/** 显示 / 隐藏评论 */
export function updateCommentVisible(
  data: MallCommentApi.CommentVisibleUpdate,
) {
  return requestClient.put('/product/comment/update-visible', data);
}

/** 商家回复 */
export function replyComment(data: MallCommentApi.CommentReply) {
  return requestClient.put('/product/comment/reply', data);
}
