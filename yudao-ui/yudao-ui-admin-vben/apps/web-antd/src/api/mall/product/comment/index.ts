import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MallCommentApi {
  /** 商品评论 */
  export interface Comment {
    id: number; // 评论编号
    userId: number; // 用户编号
    userNickname: string; // 用户昵称
    userAvatar: string; // 用户头像
    anonymous: boolean; // 是否匿名
    orderId: number; // 订单编号
    orderItemId: number; // 订单项编号
    spuId: number; // 商品SPU编号
    spuName: string; // 商品名称
    skuId: number; // 商品SKU编号
    visible: boolean; // 是否可见
    scores: number; // 总评分
    descriptionScores: number; // 描述评分
    benefitScores: number; // 服务评分
    content: string; // 评论内容
    picUrls: string[]; // 评论图片
    replyStatus: boolean; // 是否回复
    replyUserId: number; // 回复人编号
    replyContent: string; // 回复内容
    replyTime: Date; // 回复时间
    createTime: Date; // 创建时间
    skuProperties: {
      propertyId: number; // 属性 ID
      propertyName: string; // 属性名称
      valueId: number; // 属性值 ID
      valueName: string; // 属性值名称
    }[]; // SKU 属性数组
  }

  /** 评论可见性更新请求 */
  export interface CommentVisibleUpdateReqVO {
    id: number; // 评论编号
    visible: boolean; // 是否可见
  }

  /** 评论回复请求 */
  export interface CommentReplyReqVO {
    id: number; // 评论编号
    replyContent: string; // 回复内容
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
  data: MallCommentApi.CommentVisibleUpdateReqVO,
) {
  return requestClient.put('/product/comment/update-visible', data);
}

/** 商家回复 */
export function replyComment(data: MallCommentApi.CommentReplyReqVO) {
  return requestClient.put('/product/comment/reply', data);
}
