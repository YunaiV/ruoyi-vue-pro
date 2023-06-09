package cn.iocoder.yudao.module.product.dal.mysql.comment;


import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentReplyVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentAdditionalReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentPageReqVO;
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

    static void appendTabQuery(LambdaQueryWrapperX<ProductCommentDO> queryWrapper, Integer type) {
        // 构建好评查询语句
        if (ObjectUtil.equal(type, AppCommentPageReqVO.FAVOURABLE_COMMENT)) {
            // 好评计算 (商品评分星级+服务评分星级) >= 8
            queryWrapper.apply("(scores + benefit_scores) >= 8");
        }
        // 构建中评查询语句
        if (ObjectUtil.equal(type, AppCommentPageReqVO.MEDIOCRE_COMMENT)) {
            // 中评计算 (商品评分星级+服务评分星级) > 4 且 (商品评分星级+服务评分星级) < 8
            queryWrapper.apply("(scores + benefit_scores) > 4 and (scores + benefit_scores) < 8");
        }
        // 构建差评查询语句
        if (ObjectUtil.equal(type, AppCommentPageReqVO.NEGATIVE_COMMENT)) {
            // 差评计算 (商品评分星级+服务评分星级) <= 4
            queryWrapper.apply("(scores + benefit_scores) <= 4");
        }
    }

    default PageResult<ProductCommentDO> selectPage(AppCommentPageReqVO reqVO, Boolean visible) {
        LambdaQueryWrapperX<ProductCommentDO> queryWrapper = new LambdaQueryWrapperX<ProductCommentDO>()
                .eqIfPresent(ProductCommentDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(ProductCommentDO::getVisible, visible);
        // 构建评价查询语句
        appendTabQuery(queryWrapper, reqVO.getType());
        // 按评价时间排序最新的显示在前面
        queryWrapper.orderByDesc(ProductCommentDO::getCreateTime);
        return selectPage(reqVO, queryWrapper);
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

    default ProductCommentDO findByUserIdAndOrderIdAndSpuId(Long userId, Long orderId, Long spuId) {
        return selectOne(new LambdaQueryWrapperX<ProductCommentDO>()
                .eq(ProductCommentDO::getUserId, userId)
                .eq(ProductCommentDO::getOrderId, orderId)
                .eq(ProductCommentDO::getSpuId, spuId));
    }

    default void additionalComment(AppCommentAdditionalReqVO createReqVO) {
        LambdaUpdateWrapper<ProductCommentDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<ProductCommentDO>()
                .set(ProductCommentDO::getAdditionalTime, LocalDateTime.now())
                .set(ProductCommentDO::getAdditionalPicUrls, createReqVO.getAdditionalPicUrls(), "javaType=List,jdbcType=VARCHAR,typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler")
                .set(ProductCommentDO::getAdditionalContent, createReqVO.getAdditionalContent())
                .eq(ProductCommentDO::getId, createReqVO.getId());
        update(null, lambdaUpdateWrapper);
    }

    default Long selectTabCount(Long spuId, Boolean visible, Integer type) {
        LambdaQueryWrapperX<ProductCommentDO> queryWrapper = new LambdaQueryWrapperX<ProductCommentDO>()
                .eqIfPresent(ProductCommentDO::getSpuId, spuId)
                .eqIfPresent(ProductCommentDO::getVisible, visible);
        // 构建评价查询语句
        appendTabQuery(queryWrapper, type);
        return selectCount(queryWrapper);
    }

}
