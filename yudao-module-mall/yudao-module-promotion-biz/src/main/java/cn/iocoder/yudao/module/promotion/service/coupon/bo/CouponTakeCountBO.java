package cn.iocoder.yudao.module.promotion.service.coupon.bo;

import lombok.Data;

/**
 * 优惠券领取数量 BO
 *
 * @author owen
 */
@Data
public class CouponTakeCountBO {

    /**
     * 优惠劵模板编号
     */
    private Long templateId;
    /**
     * 领取数量
     */
    private Integer count;

}
