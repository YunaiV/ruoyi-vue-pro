package cn.iocoder.yudao.module.tms.enums.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * TMS 开关状态枚举
 *
 * @author wdy
 */
@RequiredArgsConstructor
@Getter
public enum TmsOffStatus implements ArrayValuable<Integer>, TmsStatusValue {

    // TMS 开关状态枚举项
    OPEN(1, "开启"),    // 开启状态
    CLOSED(2, "已关闭"), // 已关闭状态
    MANUAL_CLOSED(3, "手动关闭"); // 手动关闭状态


    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(TmsOffStatus::getCode).toArray();
    private static final Map<Integer, TmsOffStatus> STATUS_MAP = new HashMap<>();

    static {
        for (TmsOffStatus status : values()) {
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
        TmsOffStatus statusEnum = STATUS_MAP.get(code);
        return statusEnum != null ? statusEnum.getDesc() : null;
    }

    /**
     * 根据状态码获取状态枚举
     *
     * @param code 状态码
     * @return 状态枚举
     */
    public static TmsOffStatus fromCode(int code) {
        TmsOffStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的开关状态码: " + code);
        }
        return status;
    }

    @Override
    public Integer[] array() {
        return Arrays.stream(ARRAYS).boxed().toArray(Integer[]::new);
    }
} 