package cn.iocoder.yudao.framework.encrypt.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * HTTP API 加解密配置
 *
 * @author 芋道源码
 */
@ConfigurationProperties(prefix = "yudao.api-encrypt")
@Validated
@Data
public class ApiEncryptProperties {

    /**
     * 是否开启
     */
    @NotNull(message = "是否开启不能为空")
    private Boolean enable;

    /**
     * 请求头（响应头）名称
     *
     * 1. 如果该请求头非空，则表示请求参数已被「前端」加密，「后端」需要解密
     * 2. 如果该响应头非空，则表示响应结果已被「后端」加密，「前端」需要解密
     */
    @NotEmpty(message = "请求头（响应头）名称不能为空")
    private String header = "X-Api-Encrypt";

    /**
     * 对称加密算法，用于请求/响应的加解密
     *
     * 目前支持
     * 【对称加密】：
     *      1. {@link cn.hutool.crypto.symmetric.SymmetricAlgorithm#AES}
     *      2. {@link cn.hutool.crypto.symmetric.SM4#ALGORITHM_NAME} （需要自己二次开发，成本低）
     * 【非对称加密】
     *      1. {@link cn.hutool.crypto.asymmetric.AsymmetricAlgorithm#RSA}
     *      2. {@link cn.hutool.crypto.asymmetric.SM2} （需要自己二次开发，成本低）
     *
     * @see <a href="https://help.aliyun.com/zh/ssl-certificate/what-are-a-public-key-and-a-private-key">什么是公钥和私钥？</a>
     */
    @NotEmpty(message = "对称加密算法不能为空")
    private String algorithm;

    /**
     * 请求的解密密钥
     *
     * 注意：
     * 1. 如果是【对称加密】时，它「后端」对应的是“密钥”。对应的，「前端」也对应的也是“密钥”。
     * 2. 如果是【非对称加密】时，它「后端」对应的是“私钥”。对应的，「前端」对应的是“公钥”。（重要！！！）
     */
    @NotEmpty(message = "请求的解密密钥不能为空")
    private String requestKey;

    /**
     * 响应的加密密钥
     *
     * 注意：
     * 1. 如果是【对称加密】时，它「后端」对应的是“密钥”。对应的，「前端」也对应的也是“密钥”。
     * 2. 如果是【非对称加密】时，它「后端」对应的是“公钥”。对应的，「前端」对应的是“私钥”。（重要！！！）
     */
    @NotEmpty(message = "响应的加密密钥不能为空")
    private String responseKey;

}
