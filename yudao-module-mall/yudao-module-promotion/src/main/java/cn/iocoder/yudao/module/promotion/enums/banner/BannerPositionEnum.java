package cn.iocoder.yudao.module.promotion.enums.banner;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Banner Position 枚举
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum BannerPositionEnum implements ArrayValuable<Integer> {

    HOME_POSITION(1, "首页"),
    SECKILL_POSITION(2, "秒杀活动页"),
    COMBINATION_POSITION(3, "砍价活动页"),
    DISCOUNT_POSITION(4, "限时折扣页"),
    REWARD_POSITION(5, "满减送页");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BannerPositionEnum::getPosition).toArray(Integer[]::new);

    /**
     * 值
     */
    private final Integer position;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
