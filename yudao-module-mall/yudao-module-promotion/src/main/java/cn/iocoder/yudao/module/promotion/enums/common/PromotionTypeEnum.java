package cn.iocoder.yudao.module.promotion.enums.common;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 营销类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PromotionTypeEnum implements ArrayValuable<Integer> {

    SECKILL_ACTIVITY(1, "秒杀活动"),
    BARGAIN_ACTIVITY(2, "砍价活动"),
    COMBINATION_ACTIVITY(3, "拼团活动"),

    DISCOUNT_ACTIVITY(4, "限时折扣"),
    REWARD_ACTIVITY(5, "满减送"),

    MEMBER_LEVEL(6, "会员折扣"),
    COUPON(7, "优惠劵"),
    POINT(8, "积分")
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PromotionTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
