package cn.iocoder.yudao.module.iot.gateway.protocol.http;

import lombok.Data;

/**
 * IoT HTTP 协议配置
 *
 * @author 芋道源码
 */
@Data
public class IotHttpConfig {

    /**
     * 是否启用 SSL
     */
    private Boolean sslEnabled = false;

    /**
     * SSL 证书路径
     */
    private String sslCertPath;

    /**
     * SSL 私钥路径
     */
    private String sslKeyPath;

}
