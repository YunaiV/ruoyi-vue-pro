package cn.iocoder.yudao.module.product.convert.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.api.comment.dto.ProductCommentCreateReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentRespVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentStatisticsRespVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppProductCommentRespVO;
import cn.iocoder.yudao.module.product.controller.app.property.vo.value.AppProductPropertyValueDetailRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    // TODO @puhui999：这里貌似字段对上，就不用 mapping 了；可以测试下看看哈
    @Mapping(target = "goodCount", source = "goodCount")
    @Mapping(target = "mediocreCount", source = "mediocreCount")
    @Mapping(target = "negativeCount", source = "negativeCount")
    AppCommentStatisticsRespVO convert(Long goodCount, Long mediocreCount, Long negativeCount);

    List<ProductCommentRespVO> convertList(List<ProductCommentDO> list);

    List<AppProductPropertyValueDetailRespVO> convertList01(List<ProductSkuDO.Property> properties);

    PageResult<ProductCommentRespVO> convertPage(PageResult<ProductCommentDO> page);

    PageResult<AppProductCommentRespVO> convertPage02(PageResult<ProductCommentDO> pageResult);

    /**
     * 计算综合评分
     *
     * @param descriptionScores 描述星级
     * @param benefitScores     服务星级
     * @return 综合评分
     */
    @Named("convertScores")
    default Integer convertScores(Integer descriptionScores, Integer benefitScores) {
        // 计算评价最终综合评分 最终星数 = （商品评星 + 服务评星） / 2
        BigDecimal sumScore = new BigDecimal(descriptionScores + benefitScores);
        BigDecimal divide = sumScore.divide(BigDecimal.valueOf(2L), 0, RoundingMode.DOWN);
        return divide.intValue();
    }

    ProductCommentDO convert(ProductCommentCreateReqDTO createReqDTO);

    @Mapping(target = "scores", expression = "java(convertScores(createReqDTO.getDescriptionScores(), createReqDTO.getBenefitScores()))")
    default ProductCommentDO convert(ProductCommentCreateReqDTO createReqDTO, ProductSpuDO spuDO, MemberUserRespDTO user) {
        ProductCommentDO commentDO = convert(createReqDTO);
        commentDO.setUserId(user.getId());
        commentDO.setUserNickname(user.getNickname());
        commentDO.setUserAvatar(user.getAvatar());
        commentDO.setSpuId(spuDO.getId());
        commentDO.setSpuName(spuDO.getName());
        return commentDO;
    }

    @Mapping(target = "userId", constant = "0L")
    @Mapping(target = "orderId", constant = "0L")
    @Mapping(target = "orderItemId", constant = "0L")
    @Mapping(target = "anonymous", expression = "java(Boolean.FALSE)")
    @Mapping(target = "scores", expression = "java(convertScores(createReq.getDescriptionScores(), createReq.getBenefitScores()))")
    ProductCommentDO convert(ProductCommentCreateReqVO createReq);

    List<AppProductCommentRespVO> convertList02(List<ProductCommentDO> list);

}
