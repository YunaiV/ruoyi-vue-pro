package cn.iocoder.yudao.module.srm.enums.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum SrmReturnStatus implements ArrayValuable<Integer> {
    //未退款
    NOT_RETURN(0, "未退款"),
    //已退款
    RETURNED(1, "已退款"),
    //部分退款
    PART_RETURNED(2, "部分退款");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SrmReturnStatus::getCode).toArray();
    private static final Map<Integer, SrmReturnStatus> STATUS_MAP = new HashMap<>();
    // 存储状态码和描述的字段
    private final Integer code;
    private final String desc;

    @Override
    public Integer[] array() {
        int[] arrays = ARRAYS;
        Integer[] result = new Integer[arrays.length];
        for (int i = 0; i < arrays.length; i++) {
            result[i] = arrays[i];
        }
        return result;
    }


    // 根据状态码获取状态枚举
    public static SrmReturnStatus fromCode(int code) {
        SrmReturnStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的订购状态码: " + code);
        }
        return status;
    }
}
