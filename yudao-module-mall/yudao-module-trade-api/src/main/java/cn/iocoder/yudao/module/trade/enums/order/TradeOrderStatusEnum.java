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

    UNPAID(0, "待支付"),
    UNDELIVERED(10, "待发货"),
    DELIVERED(20, "已发货"),
    COMPLETED(30, "已完成"),
    CANCELED(40, "已取消");

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

    // ========== 问：为什么写了很多 isXXX 和 haveXXX 的判断逻辑呢？ ==========
    // ========== 答：方便找到某一类判断，哪些业务正在使用 ==========

    /**
     * 判断指定状态，是否正处于【未付款】状态
     *
     * @param status 指定状态
     * @return 是否
     */
    public static boolean isUnpaid(Integer status) {
        return ObjectUtil.equal(UNPAID.getStatus(), status);
    }

    /**
     * 判断指定状态，是否正处于【待发货】状态
     *
     * @param status 指定状态
     * @return 是否
     */
    public static boolean isUndelivered(Integer status) {
        return ObjectUtil.equal(UNDELIVERED.getStatus(), status);
    }

    /**
     * 判断指定状态，是否正处于【已发货】状态
     *
     * @param status 指定状态
     * @return 是否
     */
    public static boolean isDelivered(Integer status) {
        return ObjectUtil.equals(status, DELIVERED.getStatus());
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
     * 判断指定状态，是否正处于【已完成】状态
     *
     * @param status 指定状态
     * @return 是否
     */
    public static boolean isCompleted(Integer status) {
        return ObjectUtil.equals(status, COMPLETED.getStatus());
    }

    /**
     * 判断指定状态，是否有过【已付款】状态
     *
     * @param status 指定状态
     * @return 是否
     */
    public static boolean havePaid(Integer status) {
        return ObjectUtils.equalsAny(status, UNDELIVERED.getStatus(),
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
