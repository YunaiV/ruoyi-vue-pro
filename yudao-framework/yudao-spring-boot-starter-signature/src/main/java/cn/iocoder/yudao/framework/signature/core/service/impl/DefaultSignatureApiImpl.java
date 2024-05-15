package cn.iocoder.yudao.framework.signature.core.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.signature.core.service.SignatureApi;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;

/**
 * 签名的默认实现
 *
 * @author Zhougang
 */
public class DefaultSignatureApiImpl implements SignatureApi {

    @Override
    public String getAppSecret(String appId) {
        return "xxxxxxxxxx";
    }

    @Override
    public String digestEncoder(String plainText) {
        try {
            return DigestUtil.sha256Hex(StringUtils.getBytes(plainText, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
