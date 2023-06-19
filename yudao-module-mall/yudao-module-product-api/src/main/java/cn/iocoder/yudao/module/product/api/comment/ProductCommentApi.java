package cn.iocoder.yudao.module.product.api.comment;

import cn.iocoder.yudao.module.product.api.comment.dto.ProductCommentCreateReqDTO;

/**
 * 产品评论 API 接口
 *
 * @author HUIHUI
 */
public interface ProductCommentApi {

    // TODO @puhui：Long orderId 放到 createReqDTO 里噶？
    /**
     * 创建评论
     *
     * @param createReqDTO 评论参数
     * @param orderId      订单 id
     * @return 返回评论创建后的 id
     */
    Long createComment(ProductCommentCreateReqDTO createReqDTO, Long orderId);

}
