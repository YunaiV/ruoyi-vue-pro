package cn.iocoder.yudao.module.product.dal.mysql.comment;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentReplyVO;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;


/**
 * 商品评论 Mapper
 *
 * @author wangzhs
 */
@Mapper
public interface ProductCommentMapper extends BaseMapperX<ProductCommentDO> {

    default PageResult<ProductCommentDO> selectPage(ProductCommentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductCommentDO>()
                .likeIfPresent(ProductCommentDO::getUserNickname, reqVO.getUserNickname())
                .eqIfPresent(ProductCommentDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(ProductCommentDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(ProductCommentDO::getScores, reqVO.getScores())
                .betweenIfPresent(ProductCommentDO::getCreateTime, reqVO.getCreateTime())
                .likeIfPresent(ProductCommentDO::getSpuName, reqVO.getSpuName())
                .orderByDesc(ProductCommentDO::getId));
    }

    default void updateCommentVisible(Long id, Boolean visible) {
        LambdaUpdateWrapper<ProductCommentDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<ProductCommentDO>()
                .set(ProductCommentDO::getVisible, visible)
                .eq(ProductCommentDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    default void commentReply(ProductCommentReplyVO replyVO, Long loginUserId) {
        LambdaUpdateWrapper<ProductCommentDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<ProductCommentDO>()
                .set(ProductCommentDO::getReplied, Boolean.TRUE)
                .set(ProductCommentDO::getReplyTime, LocalDateTime.now())
                .set(ProductCommentDO::getReplyUserId, loginUserId)
                .set(ProductCommentDO::getReplyContent, replyVO.getReplyContent())
                .eq(ProductCommentDO::getId, replyVO.getId());
        update(null, lambdaUpdateWrapper);
    }
}
