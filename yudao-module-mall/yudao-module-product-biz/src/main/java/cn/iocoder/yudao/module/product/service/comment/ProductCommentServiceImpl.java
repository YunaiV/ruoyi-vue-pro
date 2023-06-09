package cn.iocoder.yudao.module.product.service.comment;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentReplyVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentUpdateVisibleReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentRespVO;
import cn.iocoder.yudao.module.product.convert.comment.ProductCommentConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.comment.ProductCommentMapper;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import cn.iocoder.yudao.module.trade.api.order.TradeOrderApi;
import cn.iocoder.yudao.module.trade.api.order.dto.TradeOrderRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.ORDER_NOT_FOUND;

/**
 * 商品评论 Service 实现类
 *
 * @author wangzhs
 */
@Service
@Validated
public class ProductCommentServiceImpl implements ProductCommentService {

    @Resource
    private ProductCommentMapper productCommentMapper;

    @Resource
    private TradeOrderApi tradeOrderApi;

    @Resource
    private ProductSpuService productSpuService;

    @Override
    public PageResult<ProductCommentDO> getCommentPage(ProductCommentPageReqVO pageReqVO) {
        return productCommentMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateCommentVisible(ProductCommentUpdateVisibleReqVO updateReqVO) {
        // 校验评论是否存在
        validateCommentExists(updateReqVO.getId());

        // 更新可见状态
        // TODO @puhui999：直接使用 update 操作
        productCommentMapper.updateCommentVisible(updateReqVO.getId(), updateReqVO.getVisible());
    }

    @Override
    public void commentReply(ProductCommentReplyVO replyVO, Long loginUserId) {
        // 校验评论是否存在
        validateCommentExists(replyVO.getId());

        // 回复评论
        // TODO @puhui999：直接使用 update 操作
        productCommentMapper.commentReply(replyVO, loginUserId);
    }

    @Override
    public Map<String, Long> getCommentPageTabsCount(Long spuId, Boolean visible) {
        Map<String, Long> countMap = new HashMap<>(4);
        // 查询商品 id = spuId 的所有评论数量
        countMap.put(AppCommentPageReqVO.ALL_COUNT,
                productCommentMapper.selectTabCount(spuId, visible, AppCommentPageReqVO.ALL));
        // 查询商品 id = spuId 的所有好评数量
        countMap.put(AppCommentPageReqVO.FAVOURABLE_COMMENT_COUNT,
                productCommentMapper.selectTabCount(spuId, visible, AppCommentPageReqVO.FAVOURABLE_COMMENT));
        // 查询商品 id = spuId 的所有中评数量
        countMap.put(AppCommentPageReqVO.MEDIOCRE_COMMENT_COUNT,
                productCommentMapper.selectTabCount(spuId, visible, AppCommentPageReqVO.MEDIOCRE_COMMENT));
        // 查询商品 id = spuId 的所有差评数量
        countMap.put(AppCommentPageReqVO.NEGATIVE_COMMENT_COUNT,
                productCommentMapper.selectTabCount(spuId, visible, AppCommentPageReqVO.NEGATIVE_COMMENT));
        return countMap;
    }

    @Override
    public PageResult<AppCommentRespVO> getCommentPage(AppCommentPageReqVO pageVO, Boolean visible) {
        // TODO @puhui999：逻辑可以在 controller 做哈。让 service 简介一点；因为是 view 需要不展示昵称
        PageResult<AppCommentRespVO> result = ProductCommentConvert.INSTANCE.convertPage02(productCommentMapper.selectPage(pageVO, visible));
        result.getList().forEach(item -> {
            // 判断用户是否选择匿名
            if (ObjectUtil.equal(item.getAnonymous(), true)) {
                item.setUserNickname(AppCommentPageReqVO.ANONYMOUS_NICKNAME);
            }
            // TODO @puhui999：直接插入的时候，计算到 scores 字段里；这样就去掉 finalScore 字段哈
            // 计算评价最终综合评分 最终星数 = （商品评星 + 服务评星） / 2
            BigDecimal sumScore = new BigDecimal(item.getScores() + item.getBenefitScores());
            BigDecimal divide = sumScore.divide(BigDecimal.valueOf(2L), 0, RoundingMode.DOWN);
            item.setFinalScore(divide.intValue());
        });
        return result;
    }

    @Override
    public void createComment(ProductCommentDO productComment, Boolean system) {
        // TODO @puhui999：这里不区分是否为 system；直接都校验
        if (!system) {
            // TODO 判断订单是否存在 fix；
            // TODO @puhui999：改成 order 那有个 comment 接口，哪里校验下；商品评论这里不校验订单是否存在哈
            TradeOrderRespDTO order = tradeOrderApi.getOrder(productComment.getOrderId());
            if (null == order) {
                throw exception(ORDER_NOT_FOUND);
            }
            ProductSpuDO spu = productSpuService.getSpu(productComment.getSpuId());
            if (null == spu) {
                throw exception(SPU_NOT_EXISTS);
            }
            // 判断当前订单的当前商品用户是否评价过
            ProductCommentDO exist = productCommentMapper.findByUserIdAndOrderIdAndSpuId(productComment.getId(), productComment.getOrderId(), productComment.getSpuId());
            if (null != exist) {
                throw exception(ORDER_SPU_COMMENT_EXISTS);
            }
        }
        productCommentMapper.insert(productComment);
    }

    private void validateCommentExists(Long id) {
        ProductCommentDO productComment = productCommentMapper.selectById(id);
        if (productComment == null) {
            throw exception(COMMENT_NOT_EXISTS);
        }
    }

}
