package cn.iocoder.yudao.module.iot.util;

import cn.iocoder.yudao.module.iot.enums.IotConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * TD数据库工具类
 *
 * @author AlwaysSuper
 */
@Component
public class IotTdDatabaseUtils {

    @Value("${spring.datasource.dynamic.datasource.tdengine.url}")
    private String url;

    /**
     * 获取数据库名称
     */
    public String getDatabaseName() {
        int index = url.lastIndexOf("/");
        return index != -1 ? url.substring(index + 1) : url;
    }

    /**
     * 获取产品超级表表名
     *
     * @param deviceType 设备类型
     * @param productKey 产品 Key
     * @return 产品超级表表名
     */
    public static String getProductSuperTableName(Integer deviceType, String productKey) {
        return switch (deviceType) {
            case 1 -> String.format(IotConstants.GATEWAY_SUB_STABLE_NAME_FORMAT, productKey).toLowerCase();
            case 2 -> String.format(IotConstants.GATEWAY_STABLE_NAME_FORMAT, productKey).toLowerCase();
            default -> String.format(IotConstants.DEVICE_STABLE_NAME_FORMAT, productKey).toLowerCase();
        };
    }

    /**
     * 获取物模型日志超级表表名
     *
     * @param productKey 产品 Key
     * @return 物模型日志超级表表名
     *
     */
    public static String getThinkModelMessageSuperTableName(String productKey) {
        return String.format("thing_model_message_", productKey).toLowerCase();
    }

    /**
     * 获取物模型日志设备表名
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 物模型日志设备表名
     */
    public static String getThinkModelMessageDeviceTableName(String productKey, String deviceName) {
        return String.format(IotConstants.THINK_MODEL_MESSAGE_TABLE_NAME_FORMAT, productKey.toLowerCase(), deviceName.toLowerCase());
    }

}
