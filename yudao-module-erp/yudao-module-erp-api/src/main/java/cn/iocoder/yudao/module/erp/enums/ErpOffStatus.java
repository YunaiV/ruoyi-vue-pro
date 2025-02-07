package cn.iocoder.yudao.module.erp.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum ErpOffStatus {

    OPEN(1, "开启"),
    CLOSED(2, "已关闭"),
    MANUAL_CLOSED(3, "手动关闭");

    private static final Map<Integer, ErpOffStatus> STATUS_MAP = new HashMap<>();

    static {
        // 将枚举的状态码作为键，枚举值作为值放入Map中
        for (ErpOffStatus status : values()) {
            STATUS_MAP.put(status.code, status);
        }
    }

    // 存储状态码和描述的字段
    private final int code;
    private final String description;

    // 获取状态码
    public int getCode() {
        return code;
    }

    // 获取状态描述
    public String getDescription() {
        return description;
    }

    // 根据状态码获取状态枚举
    public static ErpOffStatus fromCode(int code) {
        ErpOffStatus status = STATUS_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("无效的关闭状态码: " + code);
        }
        return status;
    }

    // 根据状态码获取对应的描述
    public static String getDescriptionByCode(Integer code) {
        ErpOffStatus status = STATUS_MAP.get(code);
        return status != null ? status.getDescription() : null;
    }
}
