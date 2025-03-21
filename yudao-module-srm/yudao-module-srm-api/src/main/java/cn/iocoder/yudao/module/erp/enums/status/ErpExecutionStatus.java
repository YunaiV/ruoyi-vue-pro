package cn.iocoder.yudao.module.erp.enums.status;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ERP 执行状态枚举
 *
 * @author fish
 */
@RequiredArgsConstructor
@Getter
public enum ErpExecutionStatus implements ArrayValuable<Integer> {

    PENDING(1, "待执行"),
    IN_PROGRESS(2, "执行中"),
    COMPLETED(3, "已完成"),
    PAUSED(4, "已暂停"),
    CANCELLED(5, "已取消"),
    FAILED(6, "执行失败"),
    ON_HOLD(7, "挂起中");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ErpExecutionStatus::getCode).toArray();
    private static final Map<Integer, ErpExecutionStatus> STATUS_MAP = new HashMap<>();

    static {
        for (ErpExecutionStatus status : values()) {
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
        ErpExecutionStatus statusEnum = STATUS_MAP.get(code);
        return statusEnum != null ? statusEnum.getDesc() : null;
    }

    /**
     * 根据状态码获取状态枚举
     *
     * @param code 状态码
     * @return 状态枚举
     */
    public static ErpExecutionStatus fromCode(int code) {
        ErpExecutionStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的执行状态码: " + code);
        }
        return status;
    }

    @Override
    public Integer[] array() {
        return Arrays.stream(ARRAYS).boxed().toArray(Integer[]::new);
    }
} 