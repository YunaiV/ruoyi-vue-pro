package cn.iocoder.yudao.module.product.api.comment;

import cn.iocoder.yudao.module.product.api.comment.dto.CommentCreateReqDTO;

/**
 * 产品评论 API 接口
 *
 * @author HUIHUI
 */
public interface ProductCommentApi {

    /**
     * 创建评论
     *
     * @param createReqDTO 评论参数
     * @param orderId      订单 id
     * @return 返回评论创建后的 id
     */
    Long createComment(CommentCreateReqDTO createReqDTO, Long orderId);

}
