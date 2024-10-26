package cn.iocoder.yudao.framework.encrypt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Zhougang
 */
@ConfigurationProperties(prefix = "yudao.encrypt")
@Data
public class EncryptProperties {

    /**
     * 是否开启，默认为 false
     */
    private boolean enable = false;
    /**
     * 私钥
     */
    private String privateKey;
    /**
     * 公钥
     */
    private String publicKey;
    /**
     * 请求头 key，客户端传递给服务端的 AES 加密密钥
     */
    private String aesKey = "AES-Key";
    /**
     * 字符集
     */
    private String charset = "UTF-8";
    /**
     * 是否打印日志
     */
    private boolean showLog = false;

}
