package cn.iocoder.yudao.module.iot.emq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// TODO @芋艿：详细再瞅瞅

/**
 * 配置类，用于读取MQTT连接的配置信息，如用户名、密码、连接地址等
 *
 * @author ahh
 */
@Data
@Component
@ConfigurationProperties("iot.emq")
public class MqttConfig {

    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;

    /**
     * 连接地址
     */
    private String hostUrl;

    /**
     * 客户Id
     */
    private String clientId;

    /**
     * 默认连接话题
     */
    private String defaultTopic;

    /**
     * 超时时间
     */
    private int timeout;

    /**
     * 保持连接数
     */
    private int keepalive;

    /**
     * 是否清除session
     */
    private boolean clearSession;

    /**
     * 是否共享订阅
     */
    private boolean isShared;

    /**
     * 分组共享订阅
     */
    private boolean isSharedGroup;

}
