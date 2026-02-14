package cn.iocoder.yudao.module.iot.core.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 序列化类型枚举
 *
 * 用于定义设备消息的序列化格式
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotSerializeTypeEnum implements ArrayValuable<String> {

    JSON("json"),
    BINARY("binary");

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotSerializeTypeEnum::getType).toArray(String[]::new);

    /**
     * 类型
     */
    private final String type;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static IotSerializeTypeEnum of(String type) {
        return ArrayUtil.firstMatch(e -> e.getType().equals(type), values());
    }

}
