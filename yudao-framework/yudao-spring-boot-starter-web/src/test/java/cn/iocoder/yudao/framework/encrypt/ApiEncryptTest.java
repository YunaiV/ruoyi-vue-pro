package cn.iocoder.yudao.framework.encrypt;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.symmetric.AES;
import org.junit.jupiter.api.Test;

import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 各种 API 加解密的测试类：校验密钥生成的配对关系，以及与 ApiEncryptFilter 一致的加解密方式。
 *
 * 不输出任何密钥、密文；如需生成密钥，可参考 {@link #testGenerateAsymmetric()} 的生成方式自行获取。
 *
 * @author 芋道源码
 */
public class ApiEncryptTest {

    private static final String BODY = "{\n" +
            "  \"username\": \"admin\",\n" +
            "  \"password\": \"admin123\",\n" +
            "  \"uuid\": \"3acd87a09a4f48fb9118333780e94883\",\n" +
            "  \"code\": \"1024\"\n" +
            "}";

    @Test
    public void testGenerateAsymmetric() {
        // 请求的密钥：前端用公钥加密，后端（requestKey）用私钥解密
        RSA requestRsa = SecureUtil.rsa();
        String requestClientKey = requestRsa.getPublicKeyBase64();
        String requestServerKey = requestRsa.getPrivateKeyBase64();
        // 响应的密钥：后端（responseKey）用公钥加密，前端用私钥解密
        RSA responseRsa = new RSA();
        String responseClientKey = responseRsa.getPrivateKeyBase64();
        String responseServerKey = responseRsa.getPublicKeyBase64();

        // 断言：请求密钥配对（与 ApiEncryptFilter 的 SecureUtil.rsa(requestKey, null) 一致）
        String requestEncrypt = SecureUtil.rsa(null, requestClientKey).encryptBase64(BODY, KeyType.PublicKey);
        assertEquals(BODY, SecureUtil.rsa(requestServerKey, null).decryptStr(requestEncrypt, KeyType.PrivateKey));
        // 断言：响应密钥配对（与 ApiEncryptFilter 的 SecureUtil.rsa(null, responseKey) 一致）
        String responseEncrypt = SecureUtil.rsa(null, responseServerKey).encryptBase64(BODY, KeyType.PublicKey);
        assertEquals(BODY, SecureUtil.rsa(responseClientKey, null).decryptStr(responseEncrypt, KeyType.PrivateKey));
        // 断言：请求、响应是两套独立的密钥
        assertNotEquals(requestClientKey, responseServerKey);
        assertNotEquals(requestServerKey, responseClientKey);
    }

    @Test
    public void testGenerateSymmetric_aes() {
        // AES 密钥可选 32、24、16 位，前后端密钥一致
        String key = RandomUtil.randomNumbers(32);
        assertEquals(32, key.length());
        AES aes = SecureUtil.aes(StrUtil.utf8Bytes(key));

        String encrypt = aes.encryptBase64(BODY);
        assertEquals(BODY, SecureUtil.aes(StrUtil.utf8Bytes(key)).decryptStr(encrypt));
    }

    @Test
    public void testEncrypt_aes() {
        String key = "52549111389893486934626385991395";
        AES aes = SecureUtil.aes(StrUtil.utf8Bytes(key));

        String encrypt = aes.encryptBase64(BODY);
        // 断言：密文为 16 字节分组，且与明文不同
        byte[] encryptBytes = Base64.decode(encrypt);
        assertEquals(0, encryptBytes.length % 16);
        assertTrue(encryptBytes.length > StrUtil.utf8Bytes(BODY).length);
        assertFalse(encrypt.contains("admin123"));
        // 断言：相同密钥下加密结果稳定，且能解密回原文
        assertEquals(encrypt, SecureUtil.aes(StrUtil.utf8Bytes(key)).encryptBase64(BODY));
        assertEquals(BODY, SecureUtil.aes(StrUtil.utf8Bytes(key)).decryptStr(encrypt));
    }

    @Test
    public void testEncrypt_rsa() {
        String key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCls2rIpnGdYnLFgz1XU13GbNQ5DloyPpvW00FPGjqn5Z6JpK+kDtVlnkhwR87iRrE5Vf2WNqRX6vzbLSgveIQY8e8oqGCb829myjf1MuI+ZzN4ghf/7tEYhZJGPI9AbfxFqBUzm+kR3/HByAI22GLT96WM26QiMK8n3tIP/yiLswIDAQAB";
        RSA rsa = SecureUtil.rsa(null, key);
        // 断言：示例公钥为 1024 位 RSA 公钥
        assertEquals(1024, ((RSAPublicKey) rsa.getPublicKey()).getModulus().bitLength());

        String encrypt = rsa.encryptBase64(BODY, KeyType.PublicKey);
        // 断言：明文不超过 117 字节，密文为一个 128 字节分组
        assertEquals(128, Base64.decode(encrypt).length);
        // 断言：PKCS#1 填充带随机数，两次加密结果不同
        assertNotEquals(encrypt, rsa.encryptBase64(BODY, KeyType.PublicKey));
    }

}
