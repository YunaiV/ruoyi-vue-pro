package cn.iocoder.yudao.module.iot.gateway.service.auth;

import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;

/**
 * IoT 设备 Token Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceTokenService {

    /**
     * 创建设备 Token
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 设备 Token
     */
    String createToken(String productKey, String deviceName);

    /**
     * 验证设备 Token
     *
     * @param token 设备 Token
     * @return 设备信息
     */
    IotDeviceIdentity verifyToken(String token);

    /**
     * 解析用户名
     *
     * @param username 用户名
     * @return 设备信息
     */
    IotDeviceIdentity parseUsername(String username);

}
