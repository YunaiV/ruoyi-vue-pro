package cn.iocoder.yudao.module.tms.enums.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * TMS 订单状态枚举
 *
 * @author wdy
 */
@RequiredArgsConstructor
@Getter
public enum TmsOrderStatus implements ArrayValuable<Integer>, TmsStatusValue {

    OT_ORDERED(1, "未订购"),
    ORDERED(2, "全部订购"),
    PARTIALLY_ORDERED(3, "部分订购"),
    ORDER_FAILED(4, "订购失败"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TmsOrderStatus::getCode).toArray();
    private static final Map<Integer, TmsOrderStatus> STATUS_MAP = new HashMap<>();

    static {
        for (TmsOrderStatus status : values()) {
            STATUS_MAP.put(status.code, status);
        }
    }

    /**
     * 状态码
     */
    private final Integer code;
    /**
     * 状态描述
     */
    private final String desc;

    /**
     * 根据状态码获取状态枚举的描述
     *
     * @param code 状态码
     * @return 状态描述，如果没有找到则返回null
     */
    public static String getDescriptionByCode(Integer code) {
        TmsOrderStatus statusEnum = STATUS_MAP.get(code);
        return statusEnum != null ? statusEnum.getDesc() : null;
    }

    /**
     * 根据状态码获取状态枚举
     *
     * @param code 状态码
     * @return 状态枚举
     */
    public static TmsOrderStatus fromCode(int code) {
        TmsOrderStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的订单状态码: " + code);
        }
        return status;
    }

    @Override
    public Integer[] array() {
        return Arrays.stream(ARRAYS).boxed().toArray(Integer[]::new);
    }
} 