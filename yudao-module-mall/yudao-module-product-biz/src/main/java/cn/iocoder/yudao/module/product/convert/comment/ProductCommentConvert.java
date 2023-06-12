package cn.iocoder.yudao.module.product.convert.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.comment.dto.CommentCreateReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentRespVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentStatisticsRespVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppProductCommentRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
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

    @Mapping(target = "allCount", source = "allCount")
    @Mapping(target = "goodCount", source = "goodCount")
    @Mapping(target = "mediocreCount", source = "mediocreCount")
    @Mapping(target = "negativeCount", source = "negativeCount")
    AppCommentStatisticsRespVO convert(Long allCount, Long goodCount, Long mediocreCount, Long negativeCount);

    List<ProductCommentRespVO> convertList(List<ProductCommentDO> list);

    PageResult<ProductCommentRespVO> convertPage(PageResult<ProductCommentDO> page);

    PageResult<AppProductCommentRespVO> convertPage02(PageResult<ProductCommentDO> pageResult);

    /**
     * 计算综合评分
     *
     * @param descriptionScores 描述星级
     * @param benefitScores     服务星级
     * @return {@link Integer}
     */
    @Named("convertScores")
    default Integer convertScores(Integer descriptionScores, Integer benefitScores) {
        // 计算评价最终综合评分 最终星数 = （商品评星 + 服务评星） / 2
        BigDecimal sumScore = new BigDecimal(descriptionScores + benefitScores);
        BigDecimal divide = sumScore.divide(BigDecimal.valueOf(2L), 0, RoundingMode.DOWN);
        return divide.intValue();
    }

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "scores", expression = "java(convertScores(createReqDTO.getDescriptionScores(), createReqDTO.getBenefitScores()))")
    ProductCommentDO convert(CommentCreateReqDTO createReqDTO, Long orderId);

    @Mapping(target = "userId", constant = "0L")
    @Mapping(target = "orderId", constant = "0L")
    @Mapping(target = "orderItemId", constant = "0L")
    @Mapping(target = "anonymous", expression = "java(Boolean.FALSE)")
    @Mapping(target = "scores", expression = "java(convertScores(createReq.getDescriptionScores(), createReq.getBenefitScores()))")
    ProductCommentDO convert(ProductCommentCreateReqVO createReq);

}
