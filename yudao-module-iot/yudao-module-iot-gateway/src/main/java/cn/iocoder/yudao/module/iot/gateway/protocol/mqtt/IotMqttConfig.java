package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * IoT 网关 MQTT 协议配置
 *
 * @author 芋道源码
 */
@Data
public class IotMqttConfig {

    /**
     * 最大消息大小（字节）
     */
    @NotNull(message = "最大消息大小不能为空")
    @Min(value = 1024, message = "最大消息大小不能小于 1024 字节")
    private Integer maxMessageSize = 8192;

    /**
     * 连接超时时间（秒）
     */
    @NotNull(message = "连接超时时间不能为空")
    @Min(value = 1, message = "连接超时时间不能小于 1 秒")
    private Integer connectTimeoutSeconds = 60;

    /**
     * 保持连接超时时间（秒）
     */
    @NotNull(message = "保持连接超时时间不能为空")
    @Min(value = 1, message = "保持连接超时时间不能小于 1 秒")
    private Integer keepAliveTimeoutSeconds = 300;

    /**
     * 是否启用 SSL
     */
    @NotNull(message = "是否启用 SSL 不能为空")
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
