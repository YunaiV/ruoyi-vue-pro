package cn.iocoder.yudao.module.oms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;

@Slf4j
@Configuration
public class OmsIntegrationConfig {


    @Bean
    public MessageChannel omsShopProductOutputChannel() {
        return new PublishSubscribeChannel(new SimpleAsyncTaskExecutor());
    }


}