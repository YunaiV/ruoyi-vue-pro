package cn.iocoder.yudao.module.iot.mqttrpc.client;

import cn.iocoder.yudao.module.iot.mqttrpc.common.RpcRequest;
import cn.iocoder.yudao.module.iot.mqttrpc.common.RpcResponse;
import cn.iocoder.yudao.module.iot.mqttrpc.common.SerializationUtils;
import cn.iocoder.yudao.module.iot.mqttrpc.config.MqttConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;
import java.util.concurrent.*;

// TODO @芋艿：需要考虑，怎么公用！
@Service
@Slf4j
public class RpcClient {

    private final MqttConfig mqttConfig;
    private final MqttClient mqttClient;
    private final ConcurrentMap<String, CompletableFuture<RpcResponse>> pendingRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public RpcClient(MqttConfig mqttConfig) throws MqttException {
        this.mqttConfig = mqttConfig;
        this.mqttClient = new MqttClient(mqttConfig.getBroker(), mqttConfig.getClientId(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setUserName(mqttConfig.getUsername());
        options.setPassword(mqttConfig.getPassword().toCharArray());
        this.mqttClient.connect(options);
    }

    @PostConstruct
    public void init() throws MqttException {
        mqttClient.subscribe(mqttConfig.getResponseTopicPrefix() + "#", this::handleResponse);
        log.info("RPC Client subscribed to topics: {}", mqttConfig.getResponseTopicPrefix() + "#");
    }

    private void handleResponse(String topic, MqttMessage message) {
        String correlationId = topic.substring(mqttConfig.getResponseTopicPrefix().length());
        RpcResponse response = SerializationUtils.deserialize(new String(message.getPayload()), RpcResponse.class);
        CompletableFuture<RpcResponse> future = pendingRequests.remove(correlationId);
        if (future != null) {
            if (response.getError() != null) {
                future.completeExceptionally(new RuntimeException(response.getError()));
            } else {
                future.complete(response);
            }
        } else {
            log.warn("Received response for unknown correlationId: {}", correlationId);
        }
    }

    public CompletableFuture<Object> call(String method, Object[] params, int timeoutSeconds) throws MqttException {
        String correlationId = UUID.randomUUID().toString();
        String replyTo = mqttConfig.getResponseTopicPrefix() + correlationId;

        RpcRequest request = new RpcRequest(method, params, correlationId, replyTo);
        String payload = SerializationUtils.serialize(request);
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        mqttClient.publish(mqttConfig.getRequestTopic(), message);

        CompletableFuture<RpcResponse> futureResponse = new CompletableFuture<>();
        pendingRequests.put(correlationId, futureResponse);

        // 设置超时
        scheduler.schedule(() -> {
            CompletableFuture<RpcResponse> removed = pendingRequests.remove(correlationId);
            if (removed != null) {
                removed.completeExceptionally(new TimeoutException("RPC call timed out"));
            }
        }, timeoutSeconds, TimeUnit.SECONDS);

        // 返回最终的结果
        return futureResponse.thenApply(RpcResponse::getResult);
    }

    @PreDestroy
    public void cleanup() throws MqttException {
        mqttClient.disconnect();
        scheduler.shutdown();
        log.info("RPC Client disconnected");
    }
}