package com.somle.esb.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

@Slf4j
public class TestHandler {

    @ServiceActivator(inputChannel = "testChannel")
    public void handleMessage(Message<String> message) {
        log.info("received message: " + message.getPayload());
    }
}
