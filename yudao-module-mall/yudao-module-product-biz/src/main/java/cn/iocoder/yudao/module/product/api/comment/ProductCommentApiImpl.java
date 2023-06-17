package cn.iocoder.yudao.module.product.api.comment;

import cn.iocoder.yudao.module.product.api.comment.dto.CommentCreateReqDTO;
import cn.iocoder.yudao.module.product.convert.comment.ProductCommentConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import cn.iocoder.yudao.module.product.service.comment.ProductCommentService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 商品评论 API 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class ProductCommentApiImpl implements ProductCommentApi {
    @Resource
    private ProductCommentService productCommentService;

    @Override
    public Long createComment(CommentCreateReqDTO createReqDTO, Long orderId) {
        ProductCommentDO commentDO = ProductCommentConvert.INSTANCE.convert(createReqDTO, orderId);
        return productCommentService.createComment(commentDO);
    }
}
