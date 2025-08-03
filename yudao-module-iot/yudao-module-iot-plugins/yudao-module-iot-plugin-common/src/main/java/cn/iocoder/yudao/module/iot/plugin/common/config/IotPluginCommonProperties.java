package cn.iocoder.yudao.module.iot.plugin.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.time.Duration;

/**
 * IoT 插件的通用配置类
 *
 * @author haohao
 */
@ConfigurationProperties(prefix = "yudao.iot.plugin.common")
@Validated
@Data
public class IotPluginCommonProperties {

    /**
     * 上行连接超时的默认值
     */
    public static final Duration UPSTREAM_CONNECT_TIMEOUT_DEFAULT = Duration.ofSeconds(30);
    /**
     * 上行读取超时的默认值
     */
    public static final Duration UPSTREAM_READ_TIMEOUT_DEFAULT = Duration.ofSeconds(30);

    /**
     * 下行端口 - 随机
     */
    public static final Integer DOWNSTREAM_PORT_RANDOM = 0;

    /**
     * 上行 URL
     */
    @NotEmpty(message = "上行 URL 不能为空")
    private String upstreamUrl;
    /**
     * 上行连接超时
     */
    private Duration upstreamConnectTimeout = UPSTREAM_CONNECT_TIMEOUT_DEFAULT;
    /**
     * 上行读取超时
     */
    private Duration upstreamReadTimeout = UPSTREAM_READ_TIMEOUT_DEFAULT;

    /**
     * 下行端口
     */
    private Integer downstreamPort = DOWNSTREAM_PORT_RANDOM;

    /**
     * 插件包标识符
     */
    @NotEmpty(message = "插件包标识符不能为空")
    private String pluginKey;

}
