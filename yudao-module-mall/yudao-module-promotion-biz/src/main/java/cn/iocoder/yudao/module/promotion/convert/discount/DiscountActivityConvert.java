package cn.iocoder.yudao.module.promotion.convert.discount;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.promotion.controller.admin.discount.vo.*;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountProductDO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionDiscountTypeEnum;
import cn.iocoder.yudao.module.promotion.service.discount.bo.DiscountProductDetailBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 限时折扣活动 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface DiscountActivityConvert {

    DiscountActivityConvert INSTANCE = Mappers.getMapper(DiscountActivityConvert.class);

    DiscountActivityDO convert(DiscountActivityCreateReqVO bean);

    DiscountActivityDO convert(DiscountActivityUpdateReqVO bean);

    DiscountActivityRespVO convert(DiscountActivityDO bean);

    List<DiscountActivityRespVO> convertList(List<DiscountActivityDO> list);

    PageResult<DiscountActivityRespVO> convertPage(PageResult<DiscountActivityDO> page);

    DiscountProductDetailBO convert(DiscountProductDO product);

    default List<DiscountProductDetailBO> convertList(List<DiscountProductDO> products, Map<Long, DiscountActivityDO> activityMap) {
        return CollectionUtils.convertList(products, product -> {
            DiscountProductDetailBO detail = convert(product);
            MapUtils.findAndThen(activityMap, product.getActivityId(), activity -> {
                detail.setActivityName(activity.getName());
            });
            return detail;
        });
    }

    DiscountProductDO convert(DiscountActivityBaseVO.Product bean);

    DiscountActivityDetailRespVO convert(DiscountActivityDO activity, List<DiscountProductDO> products);

    // =========== 比较是否相等 ==========
    /**
     * 比较两个限时折扣商品是否相等
     *
     * @param productDO 数据库中的商品
     * @param productVO 前端传入的商品
     * @return 是否匹配
     */
    @SuppressWarnings("DuplicatedCode")
    default boolean isEquals(DiscountProductDO productDO, DiscountActivityBaseVO.Product productVO) {
        if (ObjectUtil.notEqual(productDO.getSpuId(), productVO.getSpuId())
                || ObjectUtil.notEqual(productDO.getSkuId(), productVO.getSkuId())
                || ObjectUtil.notEqual(productDO.getDiscountType(), productVO.getDiscountType())) {
            return false;
        }
        if (productDO.getDiscountType().equals(PromotionDiscountTypeEnum.PRICE.getType())) {
            return ObjectUtil.equal(productDO.getDiscountPrice(), productVO.getDiscountPrice());
        }
        if (productDO.getDiscountType().equals(PromotionDiscountTypeEnum.PERCENT.getType())) {
            return ObjectUtil.equal(productDO.getDiscountPercent(), productVO.getDiscountPercent());
        }
        return true;
    }

    /**
     * 比较两个限时折扣商品是否相等
     * 注意，比较时忽略 id 编号
     *
     * @param productDO 商品 1
     * @param productVO 商品 2
     * @return 是否匹配
     */
    @SuppressWarnings("DuplicatedCode")
    default boolean isEquals(DiscountProductDO productDO, DiscountProductDO productVO) {
        if (ObjectUtil.notEqual(productDO.getSpuId(), productVO.getSpuId())
                || ObjectUtil.notEqual(productDO.getSkuId(), productVO.getSkuId())
                || ObjectUtil.notEqual(productDO.getDiscountType(), productVO.getDiscountType())) {
            return false;
        }
        if (productDO.getDiscountType().equals(PromotionDiscountTypeEnum.PRICE.getType())) {
            return ObjectUtil.equal(productDO.getDiscountPrice(), productVO.getDiscountPrice());
        }
        if (productDO.getDiscountType().equals(PromotionDiscountTypeEnum.PERCENT.getType())) {
            return ObjectUtil.equal(productDO.getDiscountPercent(), productVO.getDiscountPercent());
        }
        return true;
    }

}
