package cn.iocoder.yudao.module.iot.net.component.http.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * IoT HTTP 网络组件配置属性
 *
 * @author haohao
 */
@ConfigurationProperties(prefix = "yudao.iot.component.http")
@Validated
@Data
public class IotNetComponentHttpProperties {

    /**
     * 是否启用 HTTP 组件
     */
    private Boolean enabled;

    /**
     * HTTP 服务端口
     */
    private Integer serverPort;

    /**
     * 连接超时时间(毫秒)
     * <p>
     * 默认值：10000 毫秒
     */
    private Integer connectionTimeoutMs = 10000;
}