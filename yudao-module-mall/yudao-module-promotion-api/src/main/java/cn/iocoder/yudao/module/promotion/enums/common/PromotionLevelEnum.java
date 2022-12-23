package cn.iocoder.yudao.module.promotion.enums.common;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 营销的级别枚举
 *
 * 参考有赞：<a href="https://img01.yzcdn.cn/upload_files/2021/11/02/FhDjUrNDq-G0wjNdYDtgUX09fdGj.png">营销级别</a>
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PromotionLevelEnum implements IntArrayValuable {

    ORDER(1, "订单级"), // 多个商品，进行组合后优惠。例如说：满减送、打包一口价、第二件半价
    SKU(2, "商品级"), // 单个商品，直接优惠。例如说：限时折扣、会员折扣
    COUPON(3, "优惠劵"), // 多个商品，进行组合后优惠。例如说：优惠劵
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PromotionLevelEnum::getLevel).toArray();

    /**
     * 级别值
     */
    private final Integer level;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
