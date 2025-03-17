package cn.iocoder.yudao.module.iot.util;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

/**
 * MQTT 签名工具类
 *
 * 提供静态方法来计算 MQTT 连接参数
 */
public class MqttSignUtils {

    /**
     * 计算 MQTT 连接参数
     *
     * @param productKey   产品密钥
     * @param deviceName   设备名称
     * @param deviceSecret 设备密钥
     * @return 包含 clientId, username, password 的结果对象
     */
    public static MqttSignResult calculate(String productKey, String deviceName, String deviceSecret) {
        return calculate(productKey, deviceName, deviceSecret, productKey + "." + deviceName);
    }

    /**
     * 计算 MQTT 连接参数
     *
     * @param productKey   产品密钥
     * @param deviceName   设备名称
     * @param deviceSecret 设备密钥
     * @param clientId     客户端 ID
     * @return 包含 clientId, username, password 的结果对象
     */
    public static MqttSignResult calculate(String productKey, String deviceName, String deviceSecret, String clientId) {
        String username = deviceName + "&" + productKey;
        // 构建签名内容
        StringBuilder signContentBuilder = new StringBuilder()
                .append("clientId").append(clientId)
                .append("deviceName").append(deviceName)
                .append("deviceSecret").append(deviceSecret)
                .append("productKey").append(productKey);

        // 使用 HMac 计算签名
        byte[] key = deviceSecret.getBytes(StandardCharsets.UTF_8);
        String signContent = signContentBuilder.toString();
        HMac mac = new HMac(HmacAlgorithm.HmacSHA256, key);
        String password = mac.digestHex(signContent);

        return new MqttSignResult(clientId, username, password);
    }

    /**
     * MQTT 签名结果类
     */
    @Getter
    @AllArgsConstructor
    public static class MqttSignResult {

        private final String clientId;
        private final String username;
        private final String password;

    }

}