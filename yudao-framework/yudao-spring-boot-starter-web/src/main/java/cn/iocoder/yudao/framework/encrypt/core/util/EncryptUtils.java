package cn.iocoder.yudao.framework.encrypt.core.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author Zhougang
 */
public class EncryptUtils {

    public static final String RSA = "RSA";

    public static final String AES = "AES";

    /**
     * RSA 公钥加密
     */
    public static String encryptBase64ByRSA(String content, PublicKey publicKey) {
        RSA rsa = new RSA(null, publicKey);
        return rsa.encryptBase64(content, KeyType.PublicKey);
    }

    /**
     * RSA 公钥加密
     */
    public static String encryptBase64ByRSA(String content, String publicKey) {
        RSA rsa = new RSA(null, publicKey);
        return rsa.encryptBase64(content, KeyType.PublicKey);
    }


    /**
     * RSA 私钥解密
     */
    public static String decryptStrByRSA(String content, PrivateKey privateKey) {
        RSA rsa = new RSA(privateKey, null);
        return rsa.decryptStr(content, KeyType.PrivateKey);
    }

    /**
     * RSA 私钥解密
     */
    public static String decryptStrByRSA(String content, String privateKey) {
        RSA rsa = new RSA(privateKey, null);
        return rsa.decryptStr(content, KeyType.PrivateKey);
    }


    /**
     * AES 加密
     */
    public static String encryptBase64ByAES(String content, String key) {
        byte[] byteKey = SecureUtil.generateKey(AES, key.getBytes()).getEncoded();
        SymmetricCrypto aes = SecureUtil.aes(byteKey);
        return aes.encryptBase64(content);
    }

    /**
     * AES 解密
     */
    public static String decryptStrByAES(String encryptString, String key) {
        byte[] byteKey = SecureUtil.generateKey(AES, key.getBytes()).getEncoded();
        SymmetricCrypto aes = SecureUtil.aes(byteKey);
        return aes.decryptStr(encryptString);
    }

}

