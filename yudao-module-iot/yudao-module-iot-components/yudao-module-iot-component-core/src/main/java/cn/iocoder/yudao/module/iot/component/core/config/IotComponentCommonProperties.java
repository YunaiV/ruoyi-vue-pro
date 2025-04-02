package cn.iocoder.yudao.module.iot.component.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * IoT 组件通用配置属性
 *
 * @author haohao
 */
@ConfigurationProperties(prefix = "yudao.iot.component.core")
@Validated
@Data
public class IotComponentCommonProperties {

    /**
     * 组件的唯一标识
     * <p>
     * 注意：该值将在运行时由各组件设置，不再从配置读取
     */
    private String pluginKey;

}