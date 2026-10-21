package cn.iocoder.yudao.module.iot.framework.storage.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IoT 时序数据（属性历史、消息日志）的存储类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum IotStorageTypeEnum {

    TDENGINE("tdengine", "tdengine"),
    IOTDB("iotdb", "iotdb");

    /**
     * 类型，对应 yudao.iot.storage.type 配置项
     */
    private final String type;
    /**
     * 数据源名，对应 spring.datasource.dynamic.datasource 下的条目
     */
    private final String dataSourceName;

}
