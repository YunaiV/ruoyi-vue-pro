package cn.iocoder.yudao.module.product.convert.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentRespVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentCreateReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 商品评价 Convert
 *
 * @author wangzhs
 */
@Mapper
public interface ProductCommentConvert {

    ProductCommentConvert INSTANCE = Mappers.getMapper(ProductCommentConvert.class);

    ProductCommentRespVO convert(ProductCommentDO bean);

    List<ProductCommentRespVO> convertList(List<ProductCommentDO> list);

    PageResult<ProductCommentRespVO> convertPage(PageResult<ProductCommentDO> page);

    PageResult<AppCommentRespVO> convertPage02(PageResult<ProductCommentDO> pageResult);

    // TODO @puhui999：用 mapstruct 的映射
    default ProductCommentDO convert(MemberUserRespDTO user, AppCommentCreateReqVO createReqVO) {
        ProductCommentDO productComment = new ProductCommentDO();
        productComment.setUserId(user.getId());
        productComment.setUserNickname(user.getNickname());
        productComment.setUserAvatar(user.getAvatar());
        productComment.setAnonymous(createReqVO.getAnonymous());
        productComment.setOrderId(createReqVO.getOrderId());
        productComment.setOrderItemId(createReqVO.getOrderItemId());
        productComment.setSpuId(createReqVO.getSpuId());
        productComment.setSpuName(createReqVO.getSpuName());
        productComment.setSkuId(createReqVO.getSkuId());
        productComment.setScores(createReqVO.getScores());
        productComment.setDescriptionScores(createReqVO.getDescriptionScores());
        productComment.setBenefitScores(createReqVO.getBenefitScores());
        productComment.setContent(createReqVO.getContent());
        productComment.setPicUrls(createReqVO.getPicUrls());
        return productComment;
    }

    // TODO @puhui999：用 mapstruct 的映射
    default ProductCommentDO convert(ProductCommentCreateReqVO createReq) {
        ProductCommentDO productComment = new ProductCommentDO();
        productComment.setUserId(0L);
        productComment.setUserNickname(createReq.getUserNickname());
        productComment.setUserAvatar(createReq.getUserAvatar());
        productComment.setAnonymous(Boolean.FALSE);
        productComment.setOrderId(0L);
        productComment.setOrderItemId(0L);
        productComment.setSpuId(createReq.getSpuId());
        productComment.setSpuName(createReq.getSpuName());
        productComment.setSkuId(createReq.getSkuId());
        productComment.setScores(createReq.getScores());
        productComment.setDescriptionScores(createReq.getDescriptionScores());
        productComment.setBenefitScores(createReq.getBenefitScores());
        productComment.setContent(createReq.getContent());
        productComment.setPicUrls(createReq.getPicUrls());
        return productComment;
    }

}
