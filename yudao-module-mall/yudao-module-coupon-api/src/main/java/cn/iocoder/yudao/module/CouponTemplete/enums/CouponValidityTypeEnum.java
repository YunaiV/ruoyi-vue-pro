package cn.iocoder.yudao.module.CouponTemplete.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 过期类型 - 状态
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum CouponValidityTypeEnum {

    TIME_RANGE_EXPIRTED(1,"时间范围过期"),
    EXPIRES_AFTER_FIXED_DATE(2,"领取之日固定日期后过期"),
    EXPIRES_DATE_NEXT_FIEXD_DATE(3,"领取次日固定日期后过期"),;


    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}
