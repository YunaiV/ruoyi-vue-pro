package com.somle.dingtalk.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;

import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.DK_DEPARTMENT_CHANNEL;
import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.DK_USER_CHANNEL;

@Slf4j
@Configuration
public class DingtalkIntegrationConfig {

    @Bean(name = DK_DEPARTMENT_CHANNEL)
    public MessageChannel dingtalkDepartmentOutputChannel() {
        return new PublishSubscribeChannel(new SimpleAsyncTaskExecutor());
    }

    @Bean(name = DK_USER_CHANNEL)
    public MessageChannel dingtalkUserOutputChannel() {
        return new PublishSubscribeChannel(new SimpleAsyncTaskExecutor());
    }
}