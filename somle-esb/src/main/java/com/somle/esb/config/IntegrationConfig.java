package com.somle.esb.config;

import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import com.somle.dingtalk.service.DingTalkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.annotation.BridgeTo;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@Slf4j
@Configuration
public class IntegrationConfig {
    @Resource
    DingTalkService dingTalkService;

    @Resource
    private ConfigApi configApi;

    @Bean
    public MessageChannel testChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public MessageChannel dataChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public IntegrationFlow errorLoggingFlow() {
        return IntegrationFlow
            .from("errorChannel")
            .handle(this::logError)
            .get();
    }

    private void logError(Message<?> message) {
        Throwable exception = (Throwable) message.getPayload();
        dingTalkService.sendRobotMessage(exception.toString(), configApi.getConfigValueByKey("token.dingtalk.robot"));
    }

}