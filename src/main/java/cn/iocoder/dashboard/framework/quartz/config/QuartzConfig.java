package cn.iocoder.dashboard.framework.quartz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // 开启 Spring 自带的定时任务
public class QuartzConfig {
}
