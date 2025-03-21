package com.somle.eccang.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;

import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.EC_SALE_OUTPUT_CHANNEL;

@Slf4j
@Configuration
public class EccangIntegrationConfig {

    @Bean(name = EC_SALE_OUTPUT_CHANNEL)
    public MessageChannel eccangSaleOutputChannel() {
        return new PublishSubscribeChannel(new SimpleAsyncTaskExecutor());
    }
}