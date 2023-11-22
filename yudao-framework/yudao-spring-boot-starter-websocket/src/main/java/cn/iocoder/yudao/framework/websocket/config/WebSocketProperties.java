package cn.iocoder.yudao.framework.websocket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * WebSocket 配置项
 *
 * @author xingyu4j
 */
@ConfigurationProperties("yudao.websocket")
@Data
@Validated
public class WebSocketProperties {

    /**
     * WebSocket 的连接路径
     */
    private String path = "/ws";

}
