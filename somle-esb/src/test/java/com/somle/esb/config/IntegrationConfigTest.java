package com.somle.esb.config;

import com.somle.framework.test.core.ut.BaseSpringIntegrationTest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.support.management.SubscribableChannelManagement;
import org.springframework.messaging.MessageChannel;

@Slf4j
@Import({
    IntegrationConfig.class,
    TestHandler.class,
})
class IntegrationConfigTest extends BaseSpringIntegrationTest {

    @Resource
    MessageChannel testChannel;

//    @Qualifier("com.somle.esb.config.TestHandler.handleMessage.serviceActivator")
//    @Resource
//    EventDrivenConsumer serviceActivator;



    @Test
    void testSubscription() {
        // Send test message
        int subscriberCount = 0;
        subscriberCount = ((SubscribableChannelManagement) testChannel).getSubscriberCount();
        assert subscriberCount == 1;
    }

    @Test
    void testSend() {
        // Send test message
        testChannel.send(MessageBuilder
                .withPayload(1)
                .setHeader("type", "integer")
                .build());
    }


}