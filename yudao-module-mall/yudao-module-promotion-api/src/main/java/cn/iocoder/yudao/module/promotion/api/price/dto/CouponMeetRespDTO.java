package cn.iocoder.yudao.module.promotion.api.price.dto;

import lombok.Data;

/**
 * 优惠劵的匹配信息 Response DTO
 *
 * why 放在 price 包下？主要获取的时候，需要涉及到较多的价格计算逻辑，放在 price 可以更好的复用逻辑
 *
 * @author 芋道源码
 */
@Data
public class CouponMeetRespDTO {

    /**
     * 优惠劵编号
     */
    private Long id;

    // ========== 非优惠劵的基本信息字段 ==========
    /**
     * 是否匹配
     */
    private Boolean meet;
    /**
     * 不匹配的提示，即 {@link #meet} = true 才有值
     *
     * 例如说：
     * 1. 所结算商品没有符合条件的商品
     * 2. 差 XXX 元可用优惠劵
     * 3. 优惠劵未到使用时间
     */
    private String meetTip;

}
