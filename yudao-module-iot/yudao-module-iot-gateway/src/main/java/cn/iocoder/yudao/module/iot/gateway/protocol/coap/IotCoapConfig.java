package cn.iocoder.yudao.module.iot.gateway.protocol.coap;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * IoT CoAP 协议配置
 *
 * @author 芋道源码
 */
@Data
public class IotCoapConfig {

    /**
     * 最大消息大小（字节）
     */
    @NotNull(message = "最大消息大小不能为空")
    @Min(value = 64, message = "最大消息大小必须大于 64 字节")
    private Integer maxMessageSize = 1024;

    /**
     * ACK 超时时间（毫秒）
     */
    @NotNull(message = "ACK 超时时间不能为空")
    @Min(value = 100, message = "ACK 超时时间必须大于 100 毫秒")
    private Integer ackTimeoutMs = 2000;

    /**
     * 最大重传次数
     */
    @NotNull(message = "最大重传次数不能为空")
    @Min(value = 0, message = "最大重传次数必须大于等于 0")
    private Integer maxRetransmit = 4;

}
