package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import lombok.Data;

// TODO @AI：validator 参数校验。也看看其他几个配置类有没有类似问题
/**
 * IoT 网关 MQTT 协议配置
 *
 * @author 芋道源码
 */
@Data
public class IotMqttConfig {

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

    /**
     * 最大消息大小（字节）
     */
    private Integer maxMessageSize = 8192;

    /**
     * 连接超时时间（秒）
     */
    private Integer connectTimeoutSeconds = 60;

    /**
     * 保持连接超时时间（秒）
     */
    private Integer keepAliveTimeoutSeconds = 300;

}
