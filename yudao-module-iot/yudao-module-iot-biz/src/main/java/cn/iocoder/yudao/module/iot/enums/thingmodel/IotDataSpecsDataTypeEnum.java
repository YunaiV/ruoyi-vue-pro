package cn.iocoder.yudao.module.iot.enums.thingmodel;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 数据定义的数据类型枚举类
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum IotDataSpecsDataTypeEnum implements ArrayValuable<String> {

    INT("int"),
    FLOAT("float"),
    DOUBLE("double"),
    ENUM("enum"),
    BOOL("bool"),
    TEXT("text"),
    DATE("date"),
    STRUCT("struct"),
    ARRAY("array");

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotDataSpecsDataTypeEnum::getDataType).toArray(String[]::new);

    private final String dataType;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
