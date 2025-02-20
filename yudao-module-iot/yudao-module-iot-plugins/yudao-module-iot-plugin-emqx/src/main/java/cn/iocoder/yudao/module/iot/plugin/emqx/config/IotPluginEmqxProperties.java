package cn.iocoder.yudao.module.iot.plugin.emqx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 物联网插件 - EMQX 配置
 *
 * @author 芋道源码
 */
@ConfigurationProperties(prefix = "yudao.iot.plugin.emqx")
@Validated
@Data
public class IotPluginEmqxProperties {

    /**
     * 服务主机
     */
    private String host;

    /**
     * 服务端口
     */
    private int port;

    /**
     * 是否启用 SSL
     */
    private boolean ssl;

    /**
     * 订阅的主题
     */
    private String topics;

    /**
     * 认证端口
     */
    private int authPort;

}
