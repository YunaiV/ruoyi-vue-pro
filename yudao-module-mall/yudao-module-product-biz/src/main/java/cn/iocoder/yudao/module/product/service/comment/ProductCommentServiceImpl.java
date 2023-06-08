package cn.iocoder.yudao.module.product.service.comment;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentReplyVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentUpdateVisibleReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentAdditionalReqVO;
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
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

        productCommentMapper.updateCommentVisible(updateReqVO.getId(), updateReqVO.getVisible());
    }

    @Override
    public void commentReply(ProductCommentReplyVO replyVO, Long loginUserId) {
        // 校验评论是否存在
        validateCommentExists(replyVO.getId());

        productCommentMapper.commentReply(replyVO, loginUserId);
    }

    @Override
    public Map<String, Long> getCommentPageTabsCount(Long spuId, Boolean visible) {
        Map<String, Long> countMap = new HashMap<>(4);
        // 查询商品 id = spuId 的所有评论数量
        countMap.put(ProductCommentDO.ALL_COUNT, productCommentMapper.selectTabCount(spuId, visible, ProductCommentDO.ALL));
        // 查询商品 id = spuId 的所有好评数量
        countMap.put(ProductCommentDO.FAVOURABLE_COMMENT_COUNT, productCommentMapper.selectTabCount(spuId, visible, ProductCommentDO.FAVOURABLE_COMMENT));
        // 查询商品 id = spuId 的所有中评数量
        countMap.put(ProductCommentDO.MEDIOCRE_COMMENT_COUNT, productCommentMapper.selectTabCount(spuId, visible, ProductCommentDO.MEDIOCRE_COMMENT));
        // 查询商品 id = spuId 的所有差评数量
        countMap.put(ProductCommentDO.NEGATIVE_COMMENT_COUNT, productCommentMapper.selectTabCount(spuId, visible, ProductCommentDO.NEGATIVE_COMMENT));
        return countMap;
    }

    @Override
    public PageResult<AppCommentRespVO> getCommentPage(AppCommentPageReqVO pageVO, Boolean visible) {
        PageResult<AppCommentRespVO> result = ProductCommentConvert.INSTANCE.convertPage02(productCommentMapper.selectPage(pageVO, visible));
        result.getList().forEach(item -> {
            // 判断用户是否选择匿名
            if (ObjectUtil.equal(item.getAnonymous(), true)) {
                item.setUserNickname(ProductCommentDO.ANONYMOUS_NICKNAME);
            }
            // 计算评价最终综合评分 最终星数 = （商品评星 + 服务评星） / 2
            BigDecimal sumScore = new BigDecimal(item.getScores() + item.getBenefitScores());
            BigDecimal divide = sumScore.divide(BigDecimal.valueOf(2L), 0, RoundingMode.DOWN);
            item.setFinalScore(divide.intValue());
        });
        return result;
    }

    @Override
    public void createComment(ProductCommentDO productComment, Boolean system) {
        if (!system) {
            // TODO 判断订单是否存在 fix
            TradeOrderRespDTO order = tradeOrderApi.getOrder(productComment.getOrderId());
            if (null == order) {
                throw exception(ORDER_NOT_FOUND);
            }
            // TODO 判断 SPU 是否存在 fix
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

    @Override
    public void additionalComment(MemberUserRespDTO user, AppCommentAdditionalReqVO createReqVO) {
        // 校验评论是否存在
        ProductCommentDO productComment = validateCommentExists(createReqVO.getId());

        // 判断是否是同一用户追加评论
        if (!Objects.equals(productComment.getUserId(), user.getId())) {
            throw exception(COMMENT_ERROR_OPT);
        }

        // 判断是否已经追加评论过了
        if (StringUtils.hasText(productComment.getAdditionalContent())) {
            throw exception(COMMENT_ADDITIONAL_EXISTS);
        }

        productCommentMapper.additionalComment(createReqVO);
    }

    private ProductCommentDO validateCommentExists(Long id) {
        ProductCommentDO productComment = productCommentMapper.selectById(id);
        if (productComment == null) {
            throw exception(COMMENT_NOT_EXISTS);
        }
        return productComment;
    }


}
