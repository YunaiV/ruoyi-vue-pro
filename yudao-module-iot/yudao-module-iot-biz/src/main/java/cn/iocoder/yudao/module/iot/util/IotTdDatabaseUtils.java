package cn.iocoder.yudao.module.iot.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.enums.IotConstants;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;

// TODO @芋艿：可能要思索下，有没更好的处理方式
// TODO @芋艿：怎么改成无状态
/**
 * TD 数据库工具类
 *
 * @author AlwaysSuper
 */
public class IotTdDatabaseUtils {

    /**
     * 获取数据库名称
     */
    public static String getDatabaseName(String url) {
//       TODO @alwayssuper:StrUtil.subAfter("/")
        return StrUtil.subAfter(url, "/", true);
    }

    /**
     * 获取产品超级表表名
     *
     * @param deviceType 设备类型
     * @param productKey 产品 Key
     * @return 产品超级表表名
     */
    public static String getProductSuperTableName(Integer deviceType, String productKey) {
        Assert.notNull(deviceType, "deviceType 不能为空");
        if (IotProductDeviceTypeEnum.GATEWAY_SUB.getType().equals(deviceType)) {
            return String.format(IotConstants.GATEWAY_SUB_STABLE_NAME_FORMAT, productKey).toLowerCase();
        }
        if (IotProductDeviceTypeEnum.GATEWAY.getType().equals(deviceType)) {
            return String.format(IotConstants.GATEWAY_STABLE_NAME_FORMAT, productKey).toLowerCase();
        }
        if (IotProductDeviceTypeEnum.DIRECT.getType().equals(deviceType)){
            return String.format(IotConstants.DEVICE_STABLE_NAME_FORMAT, productKey).toLowerCase();
        }
        throw new IllegalArgumentException("deviceType 不正确");
    }

    /**
     * 获取物模型日志超级表表名
     *
     * @param productKey 产品 Key
     * @return 物模型日志超级表表名
     *
     */
    public static String getThingModelMessageSuperTableName(String productKey) {
        return "thing_model_message_" + productKey.toLowerCase();
    }

    /**
     * 获取物模型日志设备表名
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 物模型日志设备表名
     */
    public static String getThingModelMessageDeviceTableName(String productKey, String deviceName) {
        return String.format(IotConstants.THING_MODEL_MESSAGE_TABLE_NAME_FORMAT,
                productKey.toLowerCase(), deviceName.toLowerCase());
    }

}
