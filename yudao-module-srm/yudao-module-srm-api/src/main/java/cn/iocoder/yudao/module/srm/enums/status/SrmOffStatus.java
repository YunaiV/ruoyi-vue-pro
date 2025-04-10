package cn.iocoder.yudao.module.srm.enums.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum SrmOffStatus implements ArrayValuable<Integer> {

    OPEN(1, "开启"),
    CLOSED(2, "已关闭"),
    MANUAL_CLOSED(3, "手动关闭");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SrmOffStatus::getCode).toArray();
    private static final Map<Integer, SrmOffStatus> STATUS_MAP = new HashMap<>();

    static {
        // 将枚举的状态码作为键，枚举值作为值放入Map中
        for (SrmOffStatus status : values()) {
            STATUS_MAP.put(status.code, status);
        }
    }

    // 存储状态码和描述的字段
    private final Integer code;
    private final String desc;

    // 根据状态码获取状态枚举
    public static SrmOffStatus fromCode(int code) {
        SrmOffStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的关闭状态码: " + code);
        }
        return status;
    }

    // 根据状态码获取对应的描述
    public static String getDescriptionByCode(Integer code) {
        SrmOffStatus status = STATUS_MAP.get(code);
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
