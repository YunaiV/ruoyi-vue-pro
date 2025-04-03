package cn.iocoder.yudao.module.iot.component.http.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * IoT HTTP 组件配置属性
 */
@ConfigurationProperties(prefix = "yudao.iot.component.http")
@Validated
@Data
public class IotComponentHttpProperties {

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * HTTP 服务端口
     */
    private Integer serverPort;

}