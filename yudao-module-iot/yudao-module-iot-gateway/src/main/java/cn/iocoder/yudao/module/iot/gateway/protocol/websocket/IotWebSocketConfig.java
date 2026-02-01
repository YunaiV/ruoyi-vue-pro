package cn.iocoder.yudao.module.iot.gateway.protocol.websocket;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * IoT WebSocket 协议配置
 *
 * @author 芋道源码
 */
@Data
public class IotWebSocketConfig {

    /**
     * WebSocket 路径（默认：/ws）
     */
    @NotEmpty(message = "WebSocket 路径不能为空")
    private String path = "/ws";

    /**
     * 最大消息大小（字节，默认 64KB）
     */
    @NotNull(message = "最大消息大小不能为空")
    private Integer maxMessageSize = 65536;
    /**
     * 最大帧大小（字节，默认 64KB）
     */
    @NotNull(message = "最大帧大小不能为空")
    private Integer maxFrameSize = 65536;

    /**
     * 空闲超时时间（秒，默认 60）
     */
    @NotNull(message = "空闲超时时间不能为空")
    private Integer idleTimeoutSeconds = 60;

    /**
     * 是否启用 SSL（wss://）
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
