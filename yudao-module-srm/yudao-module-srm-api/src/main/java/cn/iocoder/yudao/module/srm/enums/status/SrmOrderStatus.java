package cn.iocoder.yudao.module.srm.enums.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum SrmOrderStatus implements ArrayValuable<Integer> {

    OT_ORDERED(1, "未订购"),
    ORDERED(2, "全部订购"),
    PARTIALLY_ORDERED(3, "部分订购"),
    ORDER_FAILED(4, "订购失败"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SrmOrderStatus::getCode).toArray();
    private static final Map<Integer, SrmOrderStatus> STATUS_MAP = new HashMap<>();

    static {
        // 将枚举的状态码作为键，枚举值作为值放入Map中
        for (SrmOrderStatus status : values()) {
            STATUS_MAP.put(status.code, status);
        }
    }

    // 存储状态码和描述的字段
    private final Integer code;
    private final String desc;


    // 根据状态码获取状态枚举
    public static SrmOrderStatus fromCode(int code) {
        SrmOrderStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的订购状态码: " + code);
        }
        return status;
    }

    // 根据状态码获取对应的描述
    public static String getDescriptionByCode(Integer code) {
        SrmOrderStatus status = STATUS_MAP.get(code);
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
