package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
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
        String password = buildPassword(deviceSecret,
                buildContent(clientId, productKey, deviceName, deviceSecret));
        return new AuthInfo(clientId, username, password);
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

    public static DeviceInfo parseUsername(String username) {
        String[] usernameParts = username.split("&");
        if (usernameParts.length != 2) {
            return null;
        }
        return new DeviceInfo().setProductKey(usernameParts[1]).setDeviceName(usernameParts[0]);
    }

    // ========== 动态注册相关方法 ==========

    // TODO @AI：想了下，还是放回到对应的 productService、deviceService 更合适；

    /**
     * 生成产品密钥
     *
     * @return 产品密钥（UUID）
     */
    public static String generateProductSecret() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 生成设备密钥
     *
     * @return 设备密钥（UUID）
     */
    public static String generateDeviceSecret() {
        return IdUtil.fastSimpleUUID();
    }

    // TODO @AI：去掉 random；
    /**
     * 计算动态注册签名
     * <p>
     * 参考阿里云规范，参数按字典序排列拼接
     *
     * @param productSecret 产品密钥
     * @param productKey    产品标识
     * @param deviceName    设备名称
     * @param random        随机数
     * @return 签名
     * @see <a href="https://help.aliyun.com/zh/iot/user-guide/unique-certificate-per-product-verification">一型一密</a>
     */
    public static String buildRegisterSign(String productSecret, String productKey, String deviceName, String random) {
        String content = "deviceName" + deviceName + "productKey" + productKey + "random" + random;
        return DigestUtil.hmac(HmacAlgorithm.HmacSHA256, StrUtil.utf8Bytes(productSecret))
                .digestHex(content);
    }

    // TODO @AI：是不是调用方自己验证就好了，不要这里面抽；
    /**
     * 验证动态注册签名
     *
     * @param productSecret 产品密钥
     * @param productKey    产品标识
     * @param deviceName    设备名称
     * @param random        随机数
     * @param sign          待验证的签名
     * @return 是否验证通过
     */
    public static boolean verifyRegisterSign(String productSecret, String productKey,
                                             String deviceName, String random, String sign) {
        String expectedSign = buildRegisterSign(productSecret, productKey, deviceName, random);
        return expectedSign.equals(sign);
    }

}
