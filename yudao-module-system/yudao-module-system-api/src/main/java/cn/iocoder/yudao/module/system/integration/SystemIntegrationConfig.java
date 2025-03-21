package cn.iocoder.yudao.module.system.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;

import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.DEPARTMENT_CHANNEL;

@Slf4j
@Configuration
public class SystemIntegrationConfig {

    @Bean(name = DEPARTMENT_CHANNEL)
    public MessageChannel departmentOutputChannel() {
        return new PublishSubscribeChannel(new SimpleAsyncTaskExecutor());
    }
}