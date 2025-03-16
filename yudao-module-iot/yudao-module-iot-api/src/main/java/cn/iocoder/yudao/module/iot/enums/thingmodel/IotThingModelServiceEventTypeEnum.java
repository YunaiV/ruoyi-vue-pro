package cn.iocoder.yudao.module.iot.enums.thingmodel;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 产品物模型事件类型枚举
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum IotThingModelServiceEventTypeEnum implements ArrayValuable<String> {

    INFO("info"), // 信息
    ALERT("alert"), // 告警
    ERROR("error"); // 故障

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotThingModelServiceEventTypeEnum::getType).toArray(String[]::new);

    private final String type;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
