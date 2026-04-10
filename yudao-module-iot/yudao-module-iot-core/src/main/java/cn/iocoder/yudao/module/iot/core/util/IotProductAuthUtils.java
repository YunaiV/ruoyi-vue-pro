package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;

/**
 * IoT 产品【动态注册】认证工具类
 * <p>
 * 用于一型一密场景，使用 productSecret 生成签名
 *
 * @author 芋道源码
 */
public class IotProductAuthUtils {

    /**
     * 生成设备动态注册签名
     *
     * @param productKey    产品标识
     * @param deviceName    设备名称
     * @param productSecret 产品密钥
     * @return 签名
     */
    public static String buildSign(String productKey, String deviceName, String productSecret) {
        String content = buildContent(productKey, deviceName);
        return DigestUtil.hmac(HmacAlgorithm.HmacSHA256, StrUtil.utf8Bytes(productSecret))
                .digestHex(content);
    }

    /**
     * 验证设备动态注册签名
     *
     * @param productKey    产品标识
     * @param deviceName    设备名称
     * @param productSecret 产品密钥
     * @param sign          待验证的签名
     * @return 是否验证通过
     */
    public static boolean verifySign(String productKey, String deviceName, String productSecret, String sign) {
        String expectedSign = buildSign(productKey, deviceName, productSecret);
        return expectedSign.equals(sign);
    }

    /**
     * 构建签名内容
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @return 签名内容
     */
    private static String buildContent(String productKey, String deviceName) {
        return "deviceName" + deviceName + "productKey" + productKey;
    }

}
