package cn.iocoder.yudao.module.iot.enums.plugin;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 部署方式枚举
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Getter
public enum IotPluginDeployTypeEnum implements ArrayValuable<Integer> {

    JAR(0, "JAR 部署"),
    STANDALONE(1, "独立部署");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotPluginDeployTypeEnum::getDeployType).toArray(Integer[]::new);

    /**
     * 部署方式
     */
    private final Integer deployType;
    /**
     * 部署方式名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
