package cn.iocoder.yudao.module.iot.mqttrpc.server;

import cn.hutool.core.lang.UUID;
import cn.iocoder.yudao.module.iot.mqttrpc.common.RpcRequest;
import cn.iocoder.yudao.module.iot.mqttrpc.common.RpcResponse;
import cn.iocoder.yudao.module.iot.mqttrpc.common.SerializationUtils;
import cn.iocoder.yudao.module.iot.mqttrpc.config.MqttConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

// TODO @芋艿：server 逻辑，再瞅瞅；
// TODO @haohao：如果只写在 iot biz 里，那么后续 server => client 貌似不方便？微信再讨论下~；
@Service
@Slf4j
public class RpcServer {

    private final MqttConfig mqttConfig;
    private final MqttClient mqttClient;
    private final Map<String, MethodInvoker> methodRegistry = new HashMap<>();

    public RpcServer(MqttConfig mqttConfig) throws MqttException {
        this.mqttConfig = mqttConfig;
        this.mqttClient = new MqttClient(mqttConfig.getBroker(), "rpc-server-" + UUID.randomUUID(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setUserName(mqttConfig.getUsername());
        options.setPassword(mqttConfig.getPassword().toCharArray());
        this.mqttClient.connect(options);
    }

    @PostConstruct
    public void init() throws MqttException {
        mqttClient.subscribe(mqttConfig.getRequestTopic(), this::handleRequest);
        log.info("RPC Server subscribed to topic: {}", mqttConfig.getRequestTopic());
    }

    private void handleRequest(String topic, MqttMessage message) {
        RpcRequest request = SerializationUtils.deserialize(new String(message.getPayload()), RpcRequest.class);
        RpcResponse response = new RpcResponse();
        response.setCorrelationId(request.getCorrelationId());

        try {
            MethodInvoker invoker = methodRegistry.get(request.getMethod());
            if (invoker == null) {
                throw new NoSuchMethodException("Unknown method: " + request.getMethod());
            }
            Object result = invoker.invoke(request.getParams());
            response.setResult(result);
        } catch (Exception e) {
            response.setError(e.getMessage());
            log.error("Error processing RPC request: {}", e.getMessage(), e);
        }

        String replyPayload = SerializationUtils.serialize(response);
        MqttMessage replyMessage = new MqttMessage(replyPayload.getBytes());
        replyMessage.setQos(1);
        try {
            mqttClient.publish(request.getReplyTo(), replyMessage);
            log.info("Published response to {}", request.getReplyTo());
        } catch (MqttException e) {
            log.error("Failed to publish response: {}", e.getMessage(), e);
        }
    }

    /**
     * 注册可调用的方法
     *
     * @param methodName 方法名称
     * @param invoker    方法调用器
     */
    public void registerMethod(String methodName, MethodInvoker invoker) {
        methodRegistry.put(methodName, invoker);
        log.info("Registered method: {}", methodName);
    }

    @PreDestroy
    public void cleanup() throws MqttException {
        mqttClient.disconnect();
        log.info("RPC Server disconnected");
    }

    /**
     * 方法调用器接口
     */
    @FunctionalInterface
    public interface MethodInvoker {

        Object invoke(Object[] params) throws Exception;

    }

}