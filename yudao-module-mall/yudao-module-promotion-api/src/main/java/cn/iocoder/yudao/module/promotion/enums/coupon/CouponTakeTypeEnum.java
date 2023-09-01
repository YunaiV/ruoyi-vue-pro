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

    COMMON(0, "通用"), // TODO @疯狂：要不去掉“通用"和“兑换”，保持和 crmeb 一致；就手动领取、指定发送、新人券
    BY_USER(1, "直接领取"), // 用户可在首页、每日领劵直接领取
    BY_ADMIN(2, "指定发放"), // 后台指定会员赠送优惠劵
    BY_REGISTER(3, "新人券"), // 注册时自动领取
    BY_EXCHANGE(4, "兑换"), // 一般渠道券通过兑换领取
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
