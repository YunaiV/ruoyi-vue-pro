package cn.iocoder.yudao.framework.signature.core.service;

/**
 * 签名 API 接口
 *
 * @author Zhougang
 */
public interface SignatureApi {

    /**
     * 获取appSecret
     *
     * @return appSecret
     */
    String getAppSecret(String appId);

    /**
     * 加密算法, md5, sha256
     *
     * @param plainText 需要加签的内容
     * @return sign 签名
     */
    String digestEncoder(String plainText);
}
