package cn.iocoder.yudao.module.srm.enums.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum SrmPaymentStatus implements ArrayValuable<Integer> {
    NONE_PAYMENT(1, "未付款"),
    PARTIALLY_PAYMENT(2, "部分付款"),
    ALL_PAYMENT(3, "已付款"),
    PAYMENT_EXCEPTION(4, "付款异常"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SrmPaymentStatus::getCode).toArray();
    private static final Map<Integer, SrmPaymentStatus> STATUS_MAP = new HashMap<>();

    static {
        // 将枚举的状态码作为键，枚举值作为值放入Map中
        for (SrmPaymentStatus status : values()) {
            STATUS_MAP.put(status.code, status);
        }
    }

    // 存储状态码和描述的字段
    private final Integer code;
    private final String desc;

    // 根据状态码获取状态枚举
    public static SrmPaymentStatus fromCode(int code) {
        SrmPaymentStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的订购状态码: " + code);
        }
        return status;
    }

    // 根据状态码获取对应的描述
    public static String getDescriptionByCode(Integer code) {
        SrmPaymentStatus status = STATUS_MAP.get(code);
        return status != null ? status.getDesc() : null;
    }

    /**
     * @return 数组
     */
    @Override
    public Integer[] array() {
        int[] arrays = ARRAYS;
        Integer[] result = new Integer[arrays.length];
        for (int i = 0; i < arrays.length; i++) {
            result[i] = arrays[i];
        }
        return result;
    }
}
