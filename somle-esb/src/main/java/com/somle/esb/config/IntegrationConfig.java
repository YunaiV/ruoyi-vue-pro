package com.somle.esb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.BridgeTo;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

@Configuration
public class IntegrationConfig {
    @Bean
    public MessageChannel dataChannel() {
        return new PublishSubscribeChannel();
    }
    

    // @Bean
    // public IntegrationFlow saleToData() {
    //     return IntegrationFlows.from(saleChannel())
    //             .channel(dataChannel())
    //             .get();
    // }

    
    @Bean
    public MessageChannel saleChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public MessageChannel productChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public MessageChannel departmentChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public MessageChannel syncExternalDataChannel() {
        return new PublishSubscribeChannel();
    }
}