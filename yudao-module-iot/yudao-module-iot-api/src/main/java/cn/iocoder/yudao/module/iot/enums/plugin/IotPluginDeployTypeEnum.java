package cn.iocoder.yudao.module.iot.enums.plugin;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 部署方式枚举
 *
 * @author haohao
 */
@Getter
public enum IotPluginDeployTypeEnum implements IntArrayValuable {

    UPLOAD(0, "上传jar"),
    ALONE(1, "独立运行");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotPluginDeployTypeEnum::getDeployType).toArray();

    /**
     * 部署方式
     */
    private final Integer deployType;

    /**
     * 部署方式名
     */
    private final String name;

    IotPluginDeployTypeEnum(Integer deployType, String name) {
        this.deployType = deployType;
        this.name = name;
    }

    public static IotPluginDeployTypeEnum fromDeployType(Integer deployType) {
        for (IotPluginDeployTypeEnum value : values()) {
            if (value.getDeployType().equals(deployType)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isValidDeployType(Integer deployType) {
        return fromDeployType(deployType) != null;
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
