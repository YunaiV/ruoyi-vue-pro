package cn.iocoder.yudao.module.product.service.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.api.comment.dto.ProductCommentCreateReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentReplyReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentUpdateVisibleReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentStatisticsRespVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppProductCommentRespVO;
import cn.iocoder.yudao.module.product.convert.comment.ProductCommentConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.comment.ProductCommentMapper;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

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
    private ProductSpuService productSpuService;

    @Resource
    @Lazy
    private ProductSkuService productSkuService;

    @Resource
    private MemberUserApi memberUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommentVisible(ProductCommentUpdateVisibleReqVO updateReqVO) {
        // 校验评论是否存在
        ProductCommentDO productCommentDO = validateCommentExists(updateReqVO.getId());
        productCommentDO.setVisible(updateReqVO.getVisible());

        // 更新可见状态
        productCommentMapper.updateById(productCommentDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyComment(ProductCommentReplyReqVO replyVO, Long loginUserId) {
        // 校验评论是否存在
        ProductCommentDO productCommentDO = validateCommentExists(replyVO.getId());
        productCommentDO.setReplyTime(LocalDateTime.now());
        productCommentDO.setReplyUserId(loginUserId);
        productCommentDO.setReplyStatus(Boolean.TRUE);
        productCommentDO.setReplyContent(replyVO.getReplyContent());

        // 回复评论
        productCommentMapper.updateById(productCommentDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createComment(ProductCommentCreateReqVO createReqVO) {
        // 校验评论
        validateComment(createReqVO.getSkuId(), createReqVO.getUserId(), createReqVO.getOrderItemId());

        ProductCommentDO commentDO = ProductCommentConvert.INSTANCE.convert(createReqVO);
        productCommentMapper.insert(commentDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(ProductCommentCreateReqDTO createReqDTO) {
        // 通过 sku ID 拿到 spu 相关信息
        ProductSkuDO sku = productSkuService.getSku(createReqDTO.getSkuId());
        if (sku == null) {
            throw exception(SKU_NOT_EXISTS);
        }
        // 校验 spu 如果存在返回详情
        ProductSpuDO spuDO = validateSpu(sku.getSpuId());
        // 校验评论
        validateComment(spuDO.getId(), createReqDTO.getUserId(), createReqDTO.getOrderId());
        // 获取用户详细信息
        MemberUserRespDTO user = memberUserApi.getUser(createReqDTO.getUserId());

        // 创建评论
        ProductCommentDO commentDO = ProductCommentConvert.INSTANCE.convert(createReqDTO, spuDO, user);
        productCommentMapper.insert(commentDO);
        return commentDO.getId();
    }

    private void validateComment(Long skuId, Long userId, Long orderItemId) {
        // 判断当前订单的当前商品用户是否评价过
        ProductCommentDO exist = productCommentMapper.selectByUserIdAndOrderItemIdAndSpuId(userId, orderItemId, skuId);
        if (null != exist) {
            throw exception(ORDER_SKU_COMMENT_EXISTS);
        }
    }

    private ProductSpuDO validateSpu(Long spuId) {
        ProductSpuDO spu = productSpuService.getSpu(spuId);
        if (null == spu) {
            throw exception(SPU_NOT_EXISTS);
        }
        return spu;
    }

    private ProductCommentDO validateCommentExists(Long id) {
        ProductCommentDO productComment = productCommentMapper.selectById(id);
        if (productComment == null) {
            throw exception(COMMENT_NOT_EXISTS);
        }
        return productComment;
    }

    @Override
    public AppCommentStatisticsRespVO getCommentStatistics(Long spuId, Boolean visible) {
        return ProductCommentConvert.INSTANCE.convert(
                // 查询商品 id = spuId 的所有好评数量
                productCommentMapper.selectCountBySpuId(spuId, visible, AppCommentPageReqVO.GOOD_COMMENT),
                // 查询商品 id = spuId 的所有中评数量
                productCommentMapper.selectCountBySpuId(spuId, visible, AppCommentPageReqVO.MEDIOCRE_COMMENT),
                // 查询商品 id = spuId 的所有差评数量
                productCommentMapper.selectCountBySpuId(spuId, visible, AppCommentPageReqVO.NEGATIVE_COMMENT)
        );
    }

    @Override
    public List<AppProductCommentRespVO> getCommentList(Long spuId, Integer count) {
        return ProductCommentConvert.INSTANCE.convertList02(productCommentMapper.selectCommentList(spuId, count).getList());
    }

    @Override
    public PageResult<ProductCommentDO> getCommentPage(AppCommentPageReqVO pageVO, Boolean visible) {
        return productCommentMapper.selectPage(pageVO, visible);
    }

    @Override
    public PageResult<ProductCommentDO> getCommentPage(ProductCommentPageReqVO pageReqVO) {
        return productCommentMapper.selectPage(pageReqVO);
    }

}
