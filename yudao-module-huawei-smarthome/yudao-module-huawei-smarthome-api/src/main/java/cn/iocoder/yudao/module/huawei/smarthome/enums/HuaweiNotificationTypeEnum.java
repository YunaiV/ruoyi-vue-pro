package cn.iocoder.yudao.module.huawei.smarthome.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 华为智能家居 - 事件通知类型枚举
 */
@Getter
@AllArgsConstructor
public enum HuaweiNotificationTypeEnum {

    DEVICE_EVENT("deviceEvent", "设备数据变更通知"),
    DEVICE_STATUS("deviceStatus", "设备状态变更通知 (online/offline)"),
    SCENARIO_LOG("scenarioLog", "场景执行日志"),
    DEVICE_REGISTER_STATUS("deviceRegisterStatus", "设备注册状态变更"),
    SCENARIO_IMPORT_RESULT("scenarioImportResult", "场景导入结果通知"),
    SUB_SYSTEM_DATA_EVENT("subSystemDataEvent", "子系统数据变更通知");

    private final String type;
    private final String description;

    public static HuaweiNotificationTypeEnum ofType(String type) {
        return Arrays.stream(values())
                .filter(e -> e.type.equals(type))
                .findFirst()
                .orElse(null); // 或者抛出异常，如果类型是严格的
    }
}
