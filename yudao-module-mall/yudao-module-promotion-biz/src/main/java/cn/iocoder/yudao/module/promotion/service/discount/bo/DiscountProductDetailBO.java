package cn.iocoder.yudao.module.promotion.service.discount.bo;

import lombok.Data;

/**
 * 限时折扣活动商品 BO
 *
 * @author 芋道源码
 */
@Data
public class DiscountProductDetailBO {

    // ========== DiscountProductDO 字段 ==========

    /**
     * 编号，主键自增
     */
    private Long id;
    /**
     * 限时折扣活动的编号
     */
    private Long activityId;
    /**
     * 商品 SPU 编号
     */
    private Long spuId;
    /**
     * 商品 SKU 编号
     */
    private Long skuId;
    /**
     * 折扣类型
     */
    private Integer discountType;
    /**
     * 折扣百分比
     */
    private Integer discountPercent;
    /**
     * 优惠金额，单位：分
     */
    private Integer discountPrice;

    // ========== DiscountActivityDO 字段 ==========
    /**
     * 活动标题
     */
    private String activityName;

}
