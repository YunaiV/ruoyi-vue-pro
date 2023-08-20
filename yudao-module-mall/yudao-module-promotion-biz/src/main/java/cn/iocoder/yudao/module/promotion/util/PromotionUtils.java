package cn.iocoder.yudao.module.promotion.util;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;

/**
 * 活动工具类
 *
 * @author 芋道源码
 */
public class PromotionUtils {

    /**
     * 根据时间，计算活动状态
     *
     * @param endTime 结束时间
     * @return 活动状态
     */
    public static Integer calculateActivityStatus(LocalDateTime endTime) {
        return LocalDateTimeUtils.beforeNow(endTime) ? CommonStatusEnum.DISABLE.getStatus() : CommonStatusEnum.ENABLE.getStatus();
    }

    /**
     * 校验商品 sku 是否都存在
     *
     * @param skus     数据库中的商品 skus
     * @param products 需要校验的商品
     * @param func     获取需要校验的商品的 skuId
     */
    public static <T> void validateProductSkuAllExists(List<ProductSkuRespDTO> skus, List<T> products, Function<T, Long> func) {
        // 校验 sku 个数是否一致
        Set<Long> skuIdsSet = CollectionUtils.convertSet(products, func);
        Set<Long> skuIdsSet1 = CollectionUtils.convertSet(skus, ProductSkuRespDTO::getId);
        // 校验 skuId 是否存在
        if (anyMatch(skuIdsSet, s -> !skuIdsSet1.contains(s))) {
            throw exception(SKU_NOT_EXISTS);
        }
    }

}
