package cn.iocoder.yudao.module.srm.enums.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum SrmStorageStatus implements ArrayValuable<Integer> {
    NONE_IN_STORAGE(1, "未入库"),
    PARTIALLY_IN_STORAGE(2, "部分入库"),
    ALL_IN_STORAGE(3, "全部入库"),
    ;


    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(SrmStorageStatus::getCode).toArray();
    private static final Map<Integer, SrmStorageStatus> STATUS_MAP = new HashMap<>();

    static {
        // 将枚举的状态码作为键，枚举值作为值放入Map中
        for (SrmStorageStatus status : values()) {
            STATUS_MAP.put(status.code, status);
        }
    }

    // 存储状态码和描述的字段
    private final Integer code;
    private final String desc;

    // 根据状态码获取状态枚举
    public static SrmStorageStatus fromCode(int code) {
        SrmStorageStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的订购状态码: " + code);
        }
        return status;
    }

    // 根据状态码获取对应的描述
    public static String getDescriptionByCode(Integer code) {
        SrmStorageStatus status = STATUS_MAP.get(code);
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
