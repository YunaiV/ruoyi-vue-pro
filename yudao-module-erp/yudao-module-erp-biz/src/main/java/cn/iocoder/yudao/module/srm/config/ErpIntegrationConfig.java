package cn.iocoder.yudao.module.srm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;

import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.ERP_CUSTOM_RULE_CHANNEL;
import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.ERP_PRODUCT_CHANNEL;

@Slf4j
@Configuration
public class ErpIntegrationConfig {

    @Bean(name = ERP_PRODUCT_CHANNEL)
    public MessageChannel erpProductChannel() {
        return new PublishSubscribeChannel(new SimpleAsyncTaskExecutor());
    }


    @Bean(name = ERP_CUSTOM_RULE_CHANNEL)
    public MessageChannel erpCustomRuleChannel() {
        return new PublishSubscribeChannel(new SimpleAsyncTaskExecutor());
    }
}

