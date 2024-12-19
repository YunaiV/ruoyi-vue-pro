package cn.iocoder.yudao.module.iot.enums;

/**
 * Iot 常量
 *
 * @author 芋道源码
 */
public interface IotConstants {

    /**
     * 获取设备表名
     * <p>
     * 格式为 device_{productKey}_{deviceName}
     */
    String DEVICE_TABLE_NAME_FORMAT = "device_%s_%s";

    /**
     * 获取产品属性超级表名 - 网关子设备
     * <p>
     * 格式为 gateway_sub_{productKey}
     */
    String GATEWAY_SUB_STABLE_NAME_FORMAT = "gateway_sub_%s";

    /**
     * 获取产品属性超级表名 - 网关
     * <p>
     * 格式为 gateway_{productKey}
     */
    String GATEWAY_STABLE_NAME_FORMAT = "gateway_%s";

    /**
     * 获取产品属性超级表名 - 设备
     * <p>
     * 格式为 device_{productKey}
     */
    String DEVICE_STABLE_NAME_FORMAT = "device_%s";

    /**
     * 获取物模型消息记录设备名
     * <p>
     * 格式为 thing_model_message_{productKey}_{deviceName}
     */
    String THINK_MODEL_MESSAGE_TABLE_NAME_FORMAT = "thing_model_message_%s_%s";

}