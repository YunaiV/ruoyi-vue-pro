package cn.iocoder.yudao.module.iot.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IoT 设备消息的方法枚举
 *
 * @author haohao
 */
@Getter
@AllArgsConstructor
public enum IotDeviceMessageMethodEnum {

    // ========== 设备状态 ==========

    STATE_ONLINE("thing.state.online"),
    STATE_OFFLINE("thing.state.offline"),

    ;

    private final String method;


}
