package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备【认证】的工具类，参考阿里云
 *
 * @see <a href="https://help.aliyun.com/zh/iot/user-guide/how-do-i-obtain-mqtt-parameters-for-authentication">如何计算 MQTT 签名参数</a>
 */
public class IotDeviceAuthUtils {

    /**
     * 认证信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthInfo {

        /**
         * 客户端 ID
         */
        private String clientId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 密码
         */
        private String password;

    }

    /**
     * 设备信息
     */
    @Data
    public static class DeviceInfo {

        private String productKey;

        private String deviceName;

    }

    public static AuthInfo getAuthInfo(String productKey, String deviceName, String deviceSecret) {
        String clientId = buildClientId(productKey, deviceName);
        String username = buildUsername(productKey, deviceName);
        String content = "clientId" + clientId +
                "deviceName" + deviceName +
                "deviceSecret" + deviceSecret +
                "productKey" + productKey;
        String password = buildPassword(deviceSecret, content);
        return new AuthInfo(clientId, username, password);
    }

    private static String buildClientId(String productKey, String deviceName) {
        return String.format("%s.%s", productKey, deviceName);
    }

    private static String buildUsername(String productKey, String deviceName) {
        return String.format("%s&%s", deviceName, productKey);
    }

    private static String buildPassword(String deviceSecret, String content) {
        return DigestUtil.hmac(HmacAlgorithm.HmacSHA256, deviceSecret.getBytes())
                .digestHex(content);
    }

    public static DeviceInfo parseUsername(String username) {
        String[] usernameParts = username.split("&");
        if (usernameParts.length != 2) {
            return null;
        }
        return new DeviceInfo().setProductKey(usernameParts[1]).setDeviceName(usernameParts[0]);
    }

}
