package cn.iocoder.yudao.module.iot.core.enums.opcua;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT OPC UA 安全策略枚举
 * <p>
 * 取值与 OPC UA 规范的策略名一致（Milo SecurityPolicy 的枚举名）；已废弃的 SHA-1 策略（Basic128Rsa15、Basic256）不提供
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum IotOpcUaSecurityPolicyEnum implements ArrayValuable<String> {

    NONE("None", "http://opcfoundation.org/UA/SecurityPolicy#None"),
    BASIC256_SHA256("Basic256Sha256", "http://opcfoundation.org/UA/SecurityPolicy#Basic256Sha256"),
    AES128_SHA256_RSA_OAEP("Aes128_Sha256_RsaOaep", "http://opcfoundation.org/UA/SecurityPolicy#Aes128_Sha256_RsaOaep"),
    AES256_SHA256_RSA_PSS("Aes256_Sha256_RsaPss", "http://opcfoundation.org/UA/SecurityPolicy#Aes256_Sha256_RsaPss");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotOpcUaSecurityPolicyEnum::getPolicy)
            .toArray(String[]::new);

    /**
     * 安全策略
     */
    private final String policy;
    /**
     * 安全策略 URI
     */
    private final String uri;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static IotOpcUaSecurityPolicyEnum of(String policy) {
        return ArrayUtil.firstMatch(e -> e.getPolicy().equals(policy), values());
    }

}
