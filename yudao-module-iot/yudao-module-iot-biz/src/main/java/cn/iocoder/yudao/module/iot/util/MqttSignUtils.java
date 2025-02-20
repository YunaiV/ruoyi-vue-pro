package cn.iocoder.yudao.module.iot.util;

import lombok.Getter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * MQTT 签名工具类
 * 提供静态方法来计算 MQTT 连接参数。
 */
public class MqttSignUtils {

    private static final String SIGN_METHOD = "hmacsha256";

    /**
     * 计算 MQTT 连接参数
     *
     * @param productKey   产品密钥
     * @param deviceName   设备名称
     * @param deviceSecret 设备密钥
     * @return 包含 clientId, username, password 的结果对象
     */
    public static MqttSignResult calculate(String productKey, String deviceName, String deviceSecret) {
        String clientId = productKey + "." + deviceName;
        String username = deviceName + "&" + productKey;
        String signContent = String.format("clientId%sdeviceName%sdeviceSecret%sproductKey%s",
                clientId, deviceName, deviceSecret, productKey);

        String password = sign(signContent, deviceSecret);

        return new MqttSignResult(clientId, username, password);
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
        String signContentBuilder = "clientId" + clientId +
                "deviceName" + deviceName +
                "deviceSecret" + deviceSecret +
                "productKey" + productKey;

        String password = sign(signContentBuilder, deviceSecret);

        return new MqttSignResult(clientId, username, password);
    }

    private static String sign(String content, String key) {
        try {
            Mac mac = Mac.getInstance(SIGN_METHOD);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), SIGN_METHOD));
            byte[] signData = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(signData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign content with HmacSHA256", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * MQTT 签名结果类
     */
    @Getter
    public static class MqttSignResult {
        private final String clientId;
        private final String username;
        private final String password;

        public MqttSignResult(String clientId, String username, String password) {
            this.clientId = clientId;
            this.username = username;
            this.password = password;
        }

    }
}