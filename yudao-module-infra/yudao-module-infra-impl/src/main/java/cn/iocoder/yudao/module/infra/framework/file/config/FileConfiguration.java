package cn.iocoder.yudao.module.infra.framework.file.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件 配置类
 */
@Configuration
@EnableConfigurationProperties(FileProperties.class)
public class FileConfiguration {
}
