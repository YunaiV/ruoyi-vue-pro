package cn.iocoder.yudao.module.promotion.enums.common;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
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
public enum PromotionTypeEnum implements IntArrayValuable {

    SECKILL_ACTIVITY(1, "秒杀活动"),
    BARGAIN_ACTIVITY(2, "砍价活动"),
    COMBINATION_ACTIVITY(3, "拼团活动"),

    DISCOUNT_ACTIVITY(4, "限时折扣"),
    REWARD_ACTIVITY(5, "满减送"),

    MEMBER_LEVEL(6, "会员折扣"),
    COUPON(7, "优惠劵"),
    POINT(8, "积分")
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PromotionTypeEnum::getType).toArray();

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
