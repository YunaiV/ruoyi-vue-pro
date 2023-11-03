package cn.iocoder.yudao.module.product.convert.comment;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
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
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

/**
 * 商品评价 Convert
 *
 * @author wangzhs
 */
@Mapper
public interface ProductCommentConvert {

    ProductCommentConvert INSTANCE = Mappers.getMapper(ProductCommentConvert.class);

    ProductCommentRespVO convert(ProductCommentDO bean);

    @Mapping(target = "scores", expression = "java(calculateOverallScore(goodCount, mediocreCount, negativeCount))")
    AppCommentStatisticsRespVO convert(Long goodCount, Long mediocreCount, Long negativeCount);

    @Named("calculateOverallScore")
    default double calculateOverallScore(long goodCount, long mediocreCount, long negativeCount) {
        return (goodCount * 5 + mediocreCount * 3 + negativeCount) / (double) (goodCount + mediocreCount + negativeCount);
    }

    List<ProductCommentRespVO> convertList(List<ProductCommentDO> list);

    PageResult<ProductCommentRespVO> convertPage(PageResult<ProductCommentDO> page);

    PageResult<AppProductCommentRespVO> convertPage01(PageResult<ProductCommentDO> pageResult);

    default PageResult<AppProductCommentRespVO> convertPage02(PageResult<ProductCommentDO> pageResult,
                                                              List<ProductSkuDO> skuList) {
        Map<Long, ProductSkuDO> skuMap = CollectionUtils.convertMap(skuList, ProductSkuDO::getId);
        PageResult<AppProductCommentRespVO> page = convertPage01(pageResult);
        page.getList().forEach(item -> {
            // 判断用户是否选择匿名
            if (ObjectUtil.equal(item.getAnonymous(), true)) {
                item.setUserNickname(ProductCommentDO.NICKNAME_ANONYMOUS);
            }
            // 设置 SKU 规格值
            findAndThen(skuMap, item.getSkuId(),
                    sku -> item.setSkuProperties(convertList01(sku.getProperties())));
        });
        return page;
    }

    List<AppProductPropertyValueDetailRespVO> convertList01(List<ProductSkuDO.Property> properties);

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

    @Mapping(target = "scores",
            expression = "java(convertScores(createReqDTO.getDescriptionScores(), createReqDTO.getBenefitScores()))")
    default ProductCommentDO convert(ProductCommentCreateReqDTO createReqDTO, ProductSpuDO spuDO, ProductSkuDO skuDO, MemberUserRespDTO user) {
        ProductCommentDO commentDO = convert(createReqDTO);
        if (user != null) {
            commentDO.setUserId(user.getId());
            commentDO.setUserNickname(user.getNickname());
            commentDO.setUserAvatar(user.getAvatar());
        }
        if (spuDO != null) {
            commentDO.setSpuId(spuDO.getId());
            commentDO.setSpuName(spuDO.getName());
        }
        if (skuDO != null) {
            commentDO.setSkuPicUrl(skuDO.getPicUrl());
            commentDO.setSkuProperties(skuDO.getProperties());
        }
        return commentDO;
    }

    @Mapping(target = "visible", constant = "true")
    @Mapping(target = "replyStatus", constant = "false")
    @Mapping(target = "userId", constant = "0L")
    @Mapping(target = "orderId", constant = "0L")
    @Mapping(target = "orderItemId", constant = "0L")
    @Mapping(target = "anonymous", expression = "java(Boolean.FALSE)")
    @Mapping(target = "scores",
            expression = "java(convertScores(createReq.getDescriptionScores(), createReq.getBenefitScores()))")
    ProductCommentDO convert(ProductCommentCreateReqVO createReq);

    List<AppProductCommentRespVO> convertList02(List<ProductCommentDO> list);

    default ProductCommentDO convert(ProductCommentCreateReqVO createReq, ProductSpuDO spu, ProductSkuDO sku) {
        ProductCommentDO commentDO = convert(createReq);
        if (spu != null) {
            commentDO.setSpuId(spu.getId()).setSpuName(spu.getName());
        }
        if (sku != null) {
            commentDO.setSkuPicUrl(sku.getPicUrl()).setSkuProperties(sku.getProperties());
        }
        return commentDO;
    }

}
