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
     * 路径
     */
    private String path = "";
    /**
     * 默认最多允许同时在线用户数
     */
    private int maxOnlineCount = 0;
    /**
     * 是否保存session
     */
    private boolean sessionMap = true;
}
