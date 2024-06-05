package cn.iocoder.yudao.module.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Midjourney 属性
 *
 * @author fansili
 * @time 2024/6/5 15:02
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "ai.midjourney-proxy")
@Data
public class MidjourneyProperties {

    private String key;
    private String url;
    private String notifyUrl;
}
