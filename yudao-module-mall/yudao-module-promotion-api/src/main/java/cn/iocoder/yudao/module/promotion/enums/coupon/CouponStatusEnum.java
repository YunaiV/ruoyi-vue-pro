package cn.iocoder.yudao.module.promotion.enums.coupon;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 优惠劵状态枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum CouponStatusEnum implements IntArrayValuable {

    UNUSED(1, "未使用"),
    USED(2, "已使用"),
    EXPIRE(3, "已过期"),
    // TODO @puhui999：捉摸了下，貌似搞成逻辑删除好了？不然好多地方的 status 都要做一些变动。可能未来加个 invalidateType 来标识，是管理后台删除，还是取消回收。或者优惠劵的 change log 可能更好。
    INVALID(4, "已作废");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(CouponStatusEnum::getStatus).toArray();

    /**
     * 值
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
