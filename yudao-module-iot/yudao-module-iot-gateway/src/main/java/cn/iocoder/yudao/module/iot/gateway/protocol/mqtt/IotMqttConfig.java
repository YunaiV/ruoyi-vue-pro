package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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

}
