package cn.iocoder.yudao.module.promotion.enums.coupon;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 优惠劵领取方式
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum CouponTakeTypeEnum implements IntArrayValuable {

    BY_USER(1, "直接领取"), // 用户可在首页、每日领劵直接领取
    BY_ADMIN(2, "指定发放"), // 后台指定会员赠送优惠劵
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CouponTakeTypeEnum::getValue).toArray();

    /**
     * 值
     */
    private final Integer value;
    /**
     * 名字
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
