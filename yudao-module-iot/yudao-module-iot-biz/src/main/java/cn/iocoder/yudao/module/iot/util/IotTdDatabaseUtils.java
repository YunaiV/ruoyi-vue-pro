package cn.iocoder.yudao.module.iot.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.enums.IotConstants;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_DEVICE_NOT_EXISTS;

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
        // TODO @alwayssuper：枚举字段，不要 1、2、3；不符合预期，抛出异常
        if (deviceType == null) {
            throw exception(PRODUCT_DEVICE_NOT_EXISTS);
        }
        if (IotProductDeviceTypeEnum.GATEWAY_SUB.getType().equals(deviceType)) {
            return String.format(IotConstants.GATEWAY_SUB_STABLE_NAME_FORMAT, productKey).toLowerCase();
        } else if (IotProductDeviceTypeEnum.GATEWAY.getType().equals(deviceType)) {
            return String.format(IotConstants.GATEWAY_STABLE_NAME_FORMAT, productKey).toLowerCase();
        } else if (IotProductDeviceTypeEnum.DIRECT.getType().equals(deviceType)){
            return String.format(IotConstants.DEVICE_STABLE_NAME_FORMAT, productKey).toLowerCase();
        }
        else{
            throw exception(PRODUCT_DEVICE_NOT_EXISTS);
        }
    }

    /**
     * 获取物模型日志超级表表名
     *
     * @param productKey 产品 Key
     * @return 物模型日志超级表表名
     *
     */
    public static String getThingModelMessageSuperTableName(String productKey) {
        // TODO @alwayssuper：是不是应该 + 拼接就好，不用 format
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
        return String.format(IotConstants.THING_MODEL_MESSAGE_TABLE_NAME_FORMAT, productKey.toLowerCase(), deviceName.toLowerCase());
    }

}
