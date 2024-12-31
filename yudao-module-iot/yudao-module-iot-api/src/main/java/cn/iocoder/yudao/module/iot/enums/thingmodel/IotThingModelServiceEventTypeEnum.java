package cn.iocoder.yudao.module.iot.enums.thingmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IOT 产品物模型事件类型枚举
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum IotThingModelServiceEventTypeEnum {

    INFO("info"), // 信息
    ALERT("alert"), // 告警
    ERROR("error"); // 故障

    private final String type;

}
