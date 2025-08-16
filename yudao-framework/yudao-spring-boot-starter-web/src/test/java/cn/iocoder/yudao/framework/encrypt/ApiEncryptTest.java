package cn.iocoder.yudao.framework.encrypt;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.AsymmetricAlgorithm;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Objects;

/**
 * 各种 API 加解密的测试类：不是单测，而是方便大家生成密钥、加密、解密等操作。
 *
 * @author 芋道源码
 */
@SuppressWarnings("ConstantValue")
public class ApiEncryptTest {

    @Test
    public void testGenerateAsymmetric() {
        String asymmetricAlgorithm = AsymmetricAlgorithm.RSA.getValue();
//        String asymmetricAlgorithm = "SM2";
//        String asymmetricAlgorithm = SM4.ALGORITHM_NAME;
//        String asymmetricAlgorithm = SymmetricAlgorithm.AES.getValue();
        String requestClientKey = null;
        String requestServerKey = null;
        String responseClientKey = null;
        String responseServerKey = null;
        if (Objects.equals(asymmetricAlgorithm, AsymmetricAlgorithm.RSA.getValue())) {
            // 请求的密钥
            RSA requestRsa = SecureUtil.rsa();
            requestClientKey = requestRsa.getPublicKeyBase64();
            requestServerKey = requestRsa.getPrivateKeyBase64();
            // 响应的密钥
            RSA responseRsa = new RSA();
            responseClientKey = responseRsa.getPrivateKeyBase64();
            responseServerKey = responseRsa.getPublicKeyBase64();
        } else if (Objects.equals(asymmetricAlgorithm, SymmetricAlgorithm.AES.getValue())) {
            // AES 密钥可选 32、24、16 位
            // 请求的密钥（前后端密钥一致）
            requestClientKey = RandomUtil.randomNumbers(32);
            requestServerKey = requestClientKey;
            // 响应的密钥（前后端密钥一致）
            responseClientKey = RandomUtil.randomNumbers(32);
            responseServerKey = responseClientKey;
        }

        // 打印结果
        System.out.println("requestClientKey = " + requestClientKey);
        System.out.println("requestServerKey = " + requestServerKey);
        System.out.println("responseClientKey = " + responseClientKey);
        System.out.println("responseServerKey = " + responseServerKey);
    }

    @Test
    public void testEncrypt_aes() {
        String key = "52549111389893486934626385991395";
        String body = "{\n" +
                "  \"username\": \"admin\",\n" +
                "  \"password\": \"admin123\",\n" +
                "  \"uuid\": \"3acd87a09a4f48fb9118333780e94883\",\n" +
                "  \"code\": \"1024\"\n" +
                "}";
        String encrypt = SecureUtil.aes(StrUtil.utf8Bytes(key))
                .encryptBase64(body);
        System.out.println("encrypt = " + encrypt);
    }

    @Test
    public void testEncrypt_rsa() {
        String key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCls2rIpnGdYnLFgz1XU13GbNQ5DloyPpvW00FPGjqn5Z6JpK+kDtVlnkhwR87iRrE5Vf2WNqRX6vzbLSgveIQY8e8oqGCb829myjf1MuI+ZzN4ghf/7tEYhZJGPI9AbfxFqBUzm+kR3/HByAI22GLT96WM26QiMK8n3tIP/yiLswIDAQAB";
        String body = "{\n" +
                "  \"username\": \"admin\",\n" +
                "  \"password\": \"admin123\",\n" +
                "  \"uuid\": \"3acd87a09a4f48fb9118333780e94883\",\n" +
                "  \"code\": \"1024\"\n" +
                "}";
        String encrypt = SecureUtil.rsa(null, key)
                .encryptBase64(body, KeyType.PublicKey);
        System.out.println("encrypt = " + encrypt);
    }

}
