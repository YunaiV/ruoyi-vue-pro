package cn.iocoder.yudao.framework.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "template")
public class TemplateProperties {

    /**
     * 是否启用模板预热
     */
    private boolean enablePreload;

    /**
     * 模板扫描路径（支持多个 classpath 目录）
     */
    private List<String> scanPath;

    /**
     * 缓存有效期（可选：预留未来用）
     */
    private Long cacheTtlSeconds = 86400L;
}