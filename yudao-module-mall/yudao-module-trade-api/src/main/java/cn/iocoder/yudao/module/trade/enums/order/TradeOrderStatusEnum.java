package cn.iocoder.yudao.module.trade.enums.order;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易订单 - 状态
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderStatusEnum implements IntArrayValuable {

    UNPAID(0, "未付款"),
    PAID(10, "已付款"), // 例如说，拼团订单，支付后，需要拼团成功后，才会处于待发货
    UNDELIVERED(20, "待发货"),
    DELIVERED(30, "已发货"),
    COMPLETED(40, "已完成"),
    CANCELED(50, "已取消");

    // TODO 芋艿： TAKE("待核验")：虚拟订单需要核验商品

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TradeOrderStatusEnum::getStatus).toArray();

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

    /**
     * 判断指定状态，是否正处于【已取消】状态
     *
     * @param status 指定状态
     * @return 是否
     */
    public static boolean isCanceled(Integer status) {
        return ObjectUtil.equals(status, CANCELED.getStatus());
    }

    /**
     * 判断指定状态，是否有过【已付款】状态
     *
     * @param status 指定状态
     * @return 是否
     */
    public static boolean havePaid(Integer status) {
        return ObjectUtils.equalsAny(status, PAID.getStatus(), UNDELIVERED.getStatus(),
                DELIVERED.getStatus(), COMPLETED.getStatus());
    }

    /**
     * 判断指定状态，是否有过【已发货】状态
     *
     * @param status 指定状态
     * @return 是否
     */
    public static boolean haveDelivered(Integer status) {
        return ObjectUtils.equalsAny(status, DELIVERED.getStatus(), COMPLETED.getStatus());
    }

}
