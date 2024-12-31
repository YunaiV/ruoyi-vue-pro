package cn.iocoder.yudao.module.iot.enums.thingmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IoT 数据定义的数据类型枚举类
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum IotDataSpecsDataTypeEnum {

    INT("int"),
    FLOAT("float"),
    DOUBLE("double"),
    ENUM("enum"),
    BOOL("bool"),
    TEXT("text"),
    DATE("date"),
    STRUCT("struct"),
    ARRAY("array");

    private final String dataType;

}
