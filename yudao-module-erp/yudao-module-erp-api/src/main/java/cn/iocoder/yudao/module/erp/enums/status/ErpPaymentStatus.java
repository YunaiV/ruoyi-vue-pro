package cn.iocoder.yudao.module.erp.enums.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum ErpPaymentStatus implements ArrayValuable<Integer> {
    PAYMENT_INIT(1, "未付款"),
    PARTIAL_PAYMENT(2, "部分付款"),
    COMPLETE_PAYMENT(3, "已付款"),
    PAYMENT_FAILED(4, "付款失败"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ErpPaymentStatus::getCode).toArray();
    private static final Map<Integer, ErpPaymentStatus> STATUS_MAP = new HashMap<>();

    static {
        // 将枚举的状态码作为键，枚举值作为值放入Map中
        for (ErpPaymentStatus status : values()) {
            STATUS_MAP.put(status.code, status);
        }
    }

    // 存储状态码和描述的字段
    private final Integer code;
    private final String desc;

    // 根据状态码获取状态枚举
    public static ErpPaymentStatus fromCode(int code) {
        ErpPaymentStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的订购状态码: " + code);
        }
        return status;
    }

    // 根据状态码获取对应的描述
    public static String getDescriptionByCode(Integer code) {
        ErpPaymentStatus status = STATUS_MAP.get(code);
        return status != null ? status.getDesc() : null;
    }

    /**
     * @return 数组
     */
    @Override
    public Integer[] array() {
        return Arrays.stream(ARRAYS).boxed().toArray(Integer[]::new);
    }
}
