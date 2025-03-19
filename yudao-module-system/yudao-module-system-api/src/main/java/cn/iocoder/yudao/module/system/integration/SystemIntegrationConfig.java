package cn.iocoder.yudao.module.system.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;

@Slf4j
@Configuration
public class SystemIntegrationConfig {

    @Bean
    public MessageChannel departmentOutputChannel() {
        return new PublishSubscribeChannel(new SimpleAsyncTaskExecutor());
    }
}