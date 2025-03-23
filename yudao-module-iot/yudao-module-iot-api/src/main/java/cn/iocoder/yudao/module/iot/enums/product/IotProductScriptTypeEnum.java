package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 产品脚本类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum IotProductScriptTypeEnum implements ArrayValuable<Integer> {

    PROPERTY_PARSER(1, "property_parser", "属性解析"),
    EVENT_PARSER(2, "event_parser", "事件解析"),
    COMMAND_ENCODER(3, "command_encoder", "命令编码"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotProductScriptTypeEnum::getCode)
            .toArray(Integer[]::new);

    /**
     * 编码
     */
    private final Integer code;
    /**
     * 类型
     */
    private final String type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static IotProductScriptTypeEnum getByCode(Integer code) {
        return Arrays.stream(values())
                .filter(type -> type.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}