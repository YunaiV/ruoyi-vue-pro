package com.somle.esb.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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

    @Bean
    public MessageChannel testChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public MessageChannel dataChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public MessageChannel saleChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public MessageChannel productChannel() {
        return new PublishSubscribeChannel(new SimpleAsyncTaskExecutor());
    }

    @Bean
    public MessageChannel customRuleChannel() {
        return new PublishSubscribeChannel(new SimpleAsyncTaskExecutor());
    }

    @Bean
    public MessageChannel departmentChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public IntegrationFlow productRouter() {
        return IntegrationFlow
                .from("erpProductChannel") // Incoming messages from erpProductChannel
                .channel(productChannel()) // Route to productChannel
                .get();
    }
    @Bean
    public IntegrationFlow customRuleRouter() {
        return IntegrationFlow
            .from("erpCustomRuleChannel")
            .channel(customRuleChannel())
            .get();
    }
}