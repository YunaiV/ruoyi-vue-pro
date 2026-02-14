package cn.iocoder.yudao.module.iot.gateway.protocol.udp;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * IoT UDP 协议配置
 *
 * @author 芋道源码
 */
@Data
public class IotUdpConfig {

    /**
     * 最大会话数
     */
    @NotNull(message = "最大会话数不能为空")
    @Min(value = 1, message = "最大会话数必须大于 0")
    private Integer maxSessions = 1000;
    /**
     * 会话超时时间（毫秒）
     * <p>
     * 基于 Guava Cache 的 expireAfterAccess 实现自动过期清理
     */
    @NotNull(message = "会话超时时间不能为空")
    @Min(value = 1000, message = "会话超时时间必须大于 1000 毫秒")
    private Long sessionTimeoutMs = 60000L;

    /**
     * 接收缓冲区大小（字节）
     */
    @NotNull(message = "接收缓冲区大小不能为空")
    @Min(value = 1024, message = "接收缓冲区大小必须大于 1024 字节")
    private Integer receiveBufferSize = 65536;
    /**
     * 发送缓冲区大小（字节）
     */
    @NotNull(message = "发送缓冲区大小不能为空")
    @Min(value = 1024, message = "发送缓冲区大小必须大于 1024 字节")
    private Integer sendBufferSize = 65536;

}
