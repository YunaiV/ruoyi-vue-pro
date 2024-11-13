package com.somle.esb.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;

@Slf4j
public class TestHandler {

//    @ServiceActivator(inputChannel = "testChannel")
//    public void handleObject(Object payload) {
//        if (payload instanceof String) {
//            log.info("received string: " + payload);
//        } else if (payload instanceof Integer) {
//            log.info("received integer: " + payload);
//        }
//    }

    @ServiceActivator(inputChannel = "testChannel")
    public void handleObject(Object payload, @Header("type") String type) {
        if (type.equals("string")) {
            log.info("received string: " + payload);
        } else if (type.equals("integer")) {
            log.info("received integer: " + payload);
        }
    }

//    @ServiceActivator(inputChannel = "testChannel")
//    public void handleObject(Message<Object> message) {
//        if (message.getPayload() instanceof String) {
//            log.info("received string: " + message.getPayload());
//        } else if (message.getPayload() instanceof Integer) {
//            log.info("received integer: " + message.getPayload());
//        }
//    }

//    @ServiceActivator(inputChannel = "testChannel")
//    public void handleObject(Message<Object> message) {
//        log.info("received object: " + message.getPayload());
//    }

//    @ServiceActivator(inputChannel = "testChannel")
//    public void handleString(Message<String> message) {
//        log.info("received string: " + message.getPayload());
//    }
//
//    @ServiceActivator(inputChannel = "testChannel")
//    public void handleInteger(Message<Integer> message) {
//        log.info("received integer: " + message.getPayload());
//    }
}
