package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;

/**
 * IoT 设备【认证】的工具类，参考阿里云
 *
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/how-do-i-obtain-mqtt-parameters-for-authentication">如何计算 MQTT 签名参数</a>
 */
public class IotDeviceAuthUtils {

    public static IotDeviceAuthReqDTO getAuthInfo(String productKey, String deviceName, String deviceSecret) {
        String clientId = buildClientId(productKey, deviceName);
        String username = buildUsername(productKey, deviceName);
        String password = buildPassword(deviceSecret,
                buildContent(clientId, productKey, deviceName, deviceSecret));
        return new IotDeviceAuthReqDTO(clientId, username, password);
    }

    public static String buildClientId(String productKey, String deviceName) {
        return String.format("%s.%s", productKey, deviceName);
    }

    public static String buildUsername(String productKey, String deviceName) {
        return String.format("%s&%s", deviceName, productKey);
    }

    public static String buildPassword(String deviceSecret, String content) {
        return DigestUtil.hmac(HmacAlgorithm.HmacSHA256, StrUtil.utf8Bytes(deviceSecret))
                .digestHex(content);
    }

    private static String buildContent(String clientId, String productKey, String deviceName, String deviceSecret) {
        return "clientId" + clientId +
                "deviceName" + deviceName +
                "deviceSecret" + deviceSecret +
                "productKey" + productKey;
    }

    public static IotDeviceIdentity parseUsername(String username) {
        String[] usernameParts = username.split("&");
        if (usernameParts.length != 2) {
            return null;
        }
        return new IotDeviceIdentity(usernameParts[1], usernameParts[0]);
    }

}
