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

    // TODO @haohao：参数校验，加下，啊哈

    /**
     * 服务主机
     */
    private String mqttHost;
    /**
     * 服务端口
     */
    private Integer mqttPort;
    /**
     * 服务用户名
     */
    private String mqttUsername;
    /**
     * 服务密码
     */
    private String mqttPassword;
    /**
     * 是否启用 SSL
     */
    private Boolean mqttSsl;

    /**
     * 订阅的主题列表
     */
    private String[] mqttTopics;

    /**
     * 认证端口
     */
    private Integer authPort;

}
