package cn.iocoder.yudao.module.iot.gateway.service.message;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

/**
 * IoT 设备消息 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceMessageService {

    /**
     * 编码消息
     *
     * @param message    消息
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 编码后的消息内容
     */
    byte[] encodeDeviceMessage(IotDeviceMessage message,
                               String productKey, String deviceName);

    /**
     * 解码消息
     *
     * @param bytes      消息内容
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @param serverId   设备连接的 serverId
     * @return 解码后的消息内容
     */
    IotDeviceMessage decodeDeviceMessage(byte[] bytes,
                                         String productKey, String deviceName, String serverId);

    /**
     * 构建【设备上线】消息
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @param serverId   设备连接的 serverId
     * @return 消息
     */
    IotDeviceMessage buildDeviceMessageOfStateOnline(String productKey, String deviceName, String serverId);

    /**
     * 构建【设备下线】消息
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @param serverId   设备连接的 serverId
     * @return 消息
     */
    IotDeviceMessage buildDeviceMessageOfStateOffline(String productKey, String deviceName, String serverId);

}
