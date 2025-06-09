package cn.iocoder.yudao.framework.quartz.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务 Configuration
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.task.scheduling.enabled", value = "true", matchIfMissing = false)
@EnableScheduling // 开启 Spring 自带的定时任务
@Slf4j
public class YudaoSchedulingAutoConfiguration {
}
