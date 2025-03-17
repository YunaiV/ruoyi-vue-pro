package cn.iocoder.yudao.module.iot.plugin;

import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttDisconnectMessage;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;
import io.vertx.mqtt.messages.codes.MqttSubAckReasonCode;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

// TODO @芋艿：暂未实现
/**
 * 根据官方示例，整合常见 MQTT 功能到 PF4J 的 Extension 类中
 */
@Slf4j
@Extension
public class MqttServerExtension {

    private Vertx vertx;
    private MqttServer mqttServer;

    /**
     * 启动 MQTT 服务端
     * 可根据需要决定是否启用 SSL/TLS、WebSocket、多实例部署等
     */
    public void startMqttServer() {
        // 初始化 Vert.x
        vertx = Vertx.vertx();

        // ========== 如果需要 SSL/TLS，请参考下面注释，启用注释并替换端口、证书路径等 ==========
        // MqttServerOptions options = new MqttServerOptions()
        //     .setPort(8883)
        //     .setKeyCertOptions(new PemKeyCertOptions()
        //         .setKeyPath("./src/test/resources/tls/server-key.pem")
        //         .setCertPath("./src/test/resources/tls/server-cert.pem"))
        //     .setSsl(true);

        // ========== 如果需要 WebSocket，请设置 setUseWebSocket(true) ==========
        // options.setUseWebSocket(true);

        // ========== 默认不启用 SSL 的示例 ==========
        MqttServerOptions options = new MqttServerOptions()
                .setPort(1883)
                .setHost("0.0.0.0")
                .setUseWebSocket(false); // 如果需要 WebSocket，请改为 true

        mqttServer = MqttServer.create(vertx, options);

        // 指定 endpointHandler，处理客户端连接等
        mqttServer.endpointHandler(endpoint -> {
            handleClientConnect(endpoint);
            handleDisconnect(endpoint);
            handleSubscribe(endpoint);
            handleUnsubscribe(endpoint);
            handlePublish(endpoint);
            handlePing(endpoint);
        });

        // 启动监听
        mqttServer.listen(ar -> {
            if (ar.succeeded()) {
                log.info("MQTT server is listening on port {}", mqttServer.actualPort());
            } else {
                log.error("Error on starting the server", ar.cause());
            }
        });
    }

    /**
     * 优雅关闭 MQTT 服务端
     */
    public Future<Void> stopMqttServer() {
        if (mqttServer != null) {
            return mqttServer.close().onComplete(ar -> {
                if (ar.succeeded()) {
                    log.info("MQTT server closed.");
                    if (vertx != null) {
                        vertx.close();
                        log.info("Vert.x instance closed.");
                    }
                } else {
                    log.error("Failed to close MQTT server: {}", ar.cause().getMessage());
                }
            });
        }
        return Future.succeededFuture();
    }

    // ==================== 以下为官方示例中常见事件的处理封装 ====================

    /**
     * 处理客户端连接 (CONNECT)
     */
    private void handleClientConnect(MqttEndpoint endpoint) {
        // 打印 CONNECT 的主要信息
        log.info("MQTT client [{}] request to connect, clean session = {}",
                endpoint.clientIdentifier(), endpoint.isCleanSession());

        if (endpoint.auth() != null) {
            log.info("[username = {}, password = {}]", endpoint.auth().getUsername(), endpoint.auth().getPassword());
        }
        log.info("[properties = {}]", endpoint.connectProperties());

        if (endpoint.will() != null) {
            log.info("[will topic = {}, msg = {}, QoS = {}, isRetain = {}]",
                    endpoint.will().getWillTopic(),
                    new String(endpoint.will().getWillMessageBytes()),
                    endpoint.will().getWillQos(),
                    endpoint.will().isWillRetain());
        }

        log.info("[keep alive timeout = {}]", endpoint.keepAliveTimeSeconds());

        // 接受远程客户端的连接
        endpoint.accept(false);
    }

    /**
     * 处理客户端主动断开 (DISCONNECT)
     */
    private void handleDisconnect(MqttEndpoint endpoint) {
        endpoint.disconnectMessageHandler((MqttDisconnectMessage disconnectMessage) -> {
            log.info("Received disconnect from client [{}], reason code = {}",
                    endpoint.clientIdentifier(), disconnectMessage.code());
        });
    }

    /**
     * 处理客户端订阅 (SUBSCRIBE)
     */
    private void handleSubscribe(MqttEndpoint endpoint) {
        endpoint.subscribeHandler((MqttSubscribeMessage subscribe) -> {
            List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();
            for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
                log.info("Subscription for {} with QoS {}", s.topicName(), s.qualityOfService());
                // 将客户端请求的 QoS 转换为返回给客户端的 reason code（可能是错误码或实际 granted QoS）
                reasonCodes.add(MqttSubAckReasonCode.qosGranted(s.qualityOfService()));
            }
            // 回复 SUBACK，MQTT 5.0 时可指定 reasonCodes、properties
            endpoint.subscribeAcknowledge(subscribe.messageId(), reasonCodes, MqttProperties.NO_PROPERTIES);
        });
    }

    /**
     * 处理客户端取消订阅 (UNSUBSCRIBE)
     */
    private void handleUnsubscribe(MqttEndpoint endpoint) {
        endpoint.unsubscribeHandler((MqttUnsubscribeMessage unsubscribe) -> {
            for (String topic : unsubscribe.topics()) {
                log.info("Unsubscription for {}", topic);
            }
            // 回复 UNSUBACK，MQTT 5.0 时可指定 reasonCodes、properties
            endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
        });
    }

    /**
     * 处理客户端发布的消息 (PUBLISH)
     */
    private void handlePublish(MqttEndpoint endpoint) {
        // 接收 PUBLISH 消息
        endpoint.publishHandler((MqttPublishMessage message) -> {
            String payload = message.payload().toString(Charset.defaultCharset());
            log.info("Received message [{}] on topic [{}] with QoS [{}]",
                    payload, message.topicName(), message.qosLevel());

            // 根据不同 QoS，回复客户端
            if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                endpoint.publishAcknowledge(message.messageId());
            } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                endpoint.publishReceived(message.messageId());
            }
        });

        // 如果 QoS = 2，需要处理 PUBREL
        endpoint.publishReleaseHandler(messageId -> {
            endpoint.publishComplete(messageId);
        });
    }

    /**
     * 处理客户端 PINGREQ
     */
    private void handlePing(MqttEndpoint endpoint) {
        endpoint.pingHandler(v -> {
            // 这里仅做日志, PINGRESP 已自动发送
            log.info("Ping received from client [{}]", endpoint.clientIdentifier());
        });
    }

    // ==================== 如果需要服务端向客户端发布消息，可用以下示例 ====================

    /**
     * 服务端主动向已连接的某个 endpoint 发布消息的示例
     * 如果使用 MQTT 5.0，可以传递更多消息属性
     */
    public void publishToClient(MqttEndpoint endpoint, String topic, String content) {
        endpoint.publish(topic,
                Buffer.buffer(content),
                MqttQoS.AT_LEAST_ONCE, // QoS 自行选择
                false,
                false);

        // 处理 QoS 1 和 QoS 2 的 ACK
        endpoint.publishAcknowledgeHandler(messageId -> {
            log.info("Received PUBACK from client [{}] for messageId = {}", endpoint.clientIdentifier(), messageId);
        }).publishReceivedHandler(messageId -> {
            endpoint.publishRelease(messageId);
        }).publishCompletionHandler(messageId -> {
            log.info("Received PUBCOMP from client [{}] for messageId = {}", endpoint.clientIdentifier(), messageId);
        });
    }

    // ==================== 如果需要多实例部署，用于多核扩展，可参考以下思路 ====================
    // 例如，在宿主应用或插件中循环启动多个 MqttServerExtension 实例，或使用 Vert.x 的 deployVerticle:
    // DeploymentOptions options = new DeploymentOptions().setInstances(10);
    // vertx.deployVerticle(() -> new MyMqttVerticle(), options);

}
