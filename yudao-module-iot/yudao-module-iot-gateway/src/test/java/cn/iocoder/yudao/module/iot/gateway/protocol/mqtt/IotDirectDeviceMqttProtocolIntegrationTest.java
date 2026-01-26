package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * IoT 直连设备 MQTT 协议集成测试（手动测试）
 *
 * <p>测试场景：直连设备（IotProductDeviceTypeEnum 的 DIRECT 类型）通过 MQTT 协议直接连接平台
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（MQTT 端口 1883）</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testConnect()} - 设备连接认证</li>
 *             <li>{@link #testPropertyPost()} - 设备属性上报</li>
 *             <li>{@link #testEventPost()} - 设备事件上报</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * <p>注意：MQTT 协议是有状态的长连接，认证在连接时通过 username/password 完成，
 * 认证成功后同一连接上的后续请求无需再携带认证信息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotDirectDeviceMqttProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 1883;
    private static final int TIMEOUT_SECONDS = 10;

    // ===================== 直连设备信息（根据实际情况修改，从 iot_device 表查询） =====================
    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String DEVICE_SECRET = "0baa4c2ecc104ae1a26b4070c218bdf3";

    // ===================== 全局共享 Vertx 实例 =====================
    private static Vertx vertx;

    @BeforeAll
    public static void setUp() {
        vertx = Vertx.vertx();
    }

    @AfterAll
    public static void tearDown() {
        if (vertx != null) {
            vertx.close();
        }
    }

    // ===================== 连接认证测试 =====================

    /**
     * 连接认证测试：设备通过 MQTT 协议连接平台
     */
    @Test
    public void testConnect() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        // 1. 构建认证信息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        log.info("[testConnect][认证信息: clientId={}, username={}, password={}]",
                authInfo.getClientId(), authInfo.getUsername(), authInfo.getPassword());

        // 2. 创建 MQTT 客户端配置
        MqttClientOptions options = new MqttClientOptions()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword())
                .setCleanSession(true)
                .setKeepAliveInterval(60);

        // 3. 创建 MQTT 客户端并连接
        MqttClient client = MqttClient.create(vertx, options);
        client.connect(SERVER_PORT, SERVER_HOST)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        log.info("[testConnect][连接成功，客户端 ID: {}]", client.clientId());
                        // 断开连接
                        client.disconnect()
                                .onComplete(disconnectAr -> {
                                    if (disconnectAr.succeeded()) {
                                        log.info("[testConnect][断开连接成功]");
                                    } else {
                                        log.error("[testConnect][断开连接失败]", disconnectAr.cause());
                                    }
                                    latch.countDown();
                                });
                    } else {
                        log.error("[testConnect][连接失败]", ar.cause());
                        latch.countDown();
                    }
                });

        // 4. 等待测试完成
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("[testConnect][测试超时]");
        }
    }

    // ===================== 直连设备属性上报测试 =====================

    /**
     * 属性上报测试
     */
    @Test
    public void testPropertyPost() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        // 1. 构建认证信息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);

        // 2. 创建 MQTT 客户端配置
        MqttClientOptions options = new MqttClientOptions()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword())
                .setCleanSession(true)
                .setKeepAliveInterval(60);

        // 3. 创建 MQTT 客户端并连接
        MqttClient client = MqttClient.create(vertx, options);
        client.connect(SERVER_PORT, SERVER_HOST)
                .onComplete(connectAr -> {
                    if (connectAr.succeeded()) {
                        log.info("[testPropertyPost][连接成功]");

                        // 4.1 设置消息处理器，接收 _reply 响应
                        client.publishHandler(message -> {
                            log.info("[testPropertyPost][收到响应: topic={}, payload={}]",
                                    message.topicName(), message.payload().toString());
                        });

                        // 4.2 订阅 _reply 主题
                        String replyTopic = String.format("/sys/%s/%s/thing/property/post_reply", PRODUCT_KEY, DEVICE_NAME);
                        client.subscribe(replyTopic, MqttQoS.AT_LEAST_ONCE.value())
                                .onComplete(subscribeAr -> {
                                    if (subscribeAr.succeeded()) {
                                        log.info("[testPropertyPost][订阅响应主题成功: {}]", replyTopic);

                                        // 5. 构建属性上报消息（Alink 协议格式）
                                        String topic = String.format("/sys/%s/%s/thing/property/post", PRODUCT_KEY, DEVICE_NAME);
                                        String payload = JsonUtils.toJsonString(MapUtil.builder()
                                                .put("id", IdUtil.fastSimpleUUID())
                                                .put("version", "1.0")
                                                .put("method", IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())
                                                .put("params", IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                                                        .put("width", 1)
                                                        .put("height", "2")
                                                        .build()))
                                                .build());
                                        log.info("[testPropertyPost][发送消息: topic={}, payload={}]", topic, payload);

                                        // 6. 发布消息
                                        client.publish(topic, Buffer.buffer(payload), MqttQoS.AT_LEAST_ONCE, false, false)
                                                .onComplete(publishAr -> {
                                                    if (publishAr.succeeded()) {
                                                        log.info("[testPropertyPost][消息发布成功，messageId={}]", publishAr.result());
                                                    } else {
                                                        log.error("[testPropertyPost][消息发布失败]", publishAr.cause());
                                                    }

                                                    // 等待一会儿接收响应
                                                    vertx.setTimer(2000, id -> {
                                                        client.disconnect()
                                                                .onComplete(disconnectAr -> {
                                                                    log.info("[testPropertyPost][断开连接]");
                                                                    latch.countDown();
                                                                });
                                                    });
                                                });
                                    } else {
                                        log.error("[testPropertyPost][订阅响应主题失败]", subscribeAr.cause());
                                        latch.countDown();
                                    }
                                });
                    } else {
                        log.error("[testPropertyPost][连接失败]", connectAr.cause());
                        latch.countDown();
                    }
                });

        // 7. 等待测试完成
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("[testPropertyPost][测试超时]");
        }
    }

    // ===================== 直连设备事件上报测试 =====================

    /**
     * 事件上报测试
     */
    @Test
    public void testEventPost() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        // 1. 构建认证信息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);

        // 2. 创建 MQTT 客户端配置
        MqttClientOptions options = new MqttClientOptions()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword())
                .setCleanSession(true)
                .setKeepAliveInterval(60);

        // 3. 创建 MQTT 客户端并连接
        MqttClient client = MqttClient.create(vertx, options);
        // TODO @AI：可以像 tcp 里面一样，有个复用么？auth 流程；
        client.connect(SERVER_PORT, SERVER_HOST)
                .onComplete(connectAr -> {
                    if (connectAr.succeeded()) {
                        log.info("[testEventPost][连接成功]");

                        // 4.1 设置消息处理器，接收 _reply 响应
                        client.publishHandler(message -> {
                            log.info("[testEventPost][收到响应: topic={}, payload={}]",
                                    message.topicName(), message.payload().toString());
                        });

                        // 4.2 订阅 _reply 主题
                        String replyTopic = String.format("/sys/%s/%s/thing/event/post_reply", PRODUCT_KEY, DEVICE_NAME);
                        client.subscribe(replyTopic, MqttQoS.AT_LEAST_ONCE.value())
                                .onComplete(subscribeAr -> {
                                    if (subscribeAr.succeeded()) {
                                        log.info("[testEventPost][订阅响应主题成功: {}]", replyTopic);

                                        // 5. 构建事件上报消息（Alink 协议格式）
                                        String topic = String.format("/sys/%s/%s/thing/event/post", PRODUCT_KEY, DEVICE_NAME);
                                        String payload = JsonUtils.toJsonString(MapUtil.builder()
                                                .put("id", IdUtil.fastSimpleUUID())
                                                .put("version", "1.0")
                                                .put("method", IotDeviceMessageMethodEnum.EVENT_POST.getMethod())
                                                .put("params", IotDeviceEventPostReqDTO.of(
                                                        "eat",
                                                        MapUtil.<String, Object>builder().put("rice", 3).build(),
                                                        System.currentTimeMillis()))
                                                .build());
                                        log.info("[testEventPost][发送消息: topic={}, payload={}]", topic, payload);

                                        // 6. 发布消息
                                        client.publish(topic, Buffer.buffer(payload), MqttQoS.AT_LEAST_ONCE, false, false)
                                                .onComplete(publishAr -> {
                                                    if (publishAr.succeeded()) {
                                                        log.info("[testEventPost][消息发布成功，messageId={}]", publishAr.result());
                                                    } else {
                                                        log.error("[testEventPost][消息发布失败]", publishAr.cause());
                                                    }

                                                    // 等待一会儿接收响应
                                                    vertx.setTimer(2000, id -> {
                                                        client.disconnect()
                                                                .onComplete(disconnectAr -> {
                                                                    log.info("[testEventPost][断开连接]");
                                                                    latch.countDown();
                                                                });
                                                    });
                                                });
                                    } else {
                                        log.error("[testEventPost][订阅响应主题失败]", subscribeAr.cause());
                                        latch.countDown();
                                    }
                                });
                    } else {
                        log.error("[testEventPost][连接失败]", connectAr.cause());
                        latch.countDown();
                    }
                });

        // 7. 等待测试完成
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("[testEventPost][测试超时]");
        }
    }

    // ===================== 订阅下行消息测试 =====================

    /**
     * 订阅下行消息测试：订阅服务端下发的消息
     */
    @Test
    public void testSubscribe() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        // 1. 构建认证信息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);

        // 2. 创建 MQTT 客户端配置
        MqttClientOptions options = new MqttClientOptions()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword())
                .setCleanSession(true)
                .setKeepAliveInterval(60);

        // 3. 创建 MQTT 客户端并连接
        MqttClient client = MqttClient.create(vertx, options);
        client.connect(SERVER_PORT, SERVER_HOST)
                .onComplete(connectAr -> {
                    if (connectAr.succeeded()) {
                        log.info("[testSubscribe][连接成功]");

                        // 4. 设置消息处理器
                        client.publishHandler(message -> {
                            log.info("[testSubscribe][收到消息: topic={}, payload={}]",
                                    message.topicName(), message.payload().toString());
                        });

                        // 5. 订阅下行主题
                        String topic = String.format("/sys/%s/%s/thing/service/#", PRODUCT_KEY, DEVICE_NAME);
                        log.info("[testSubscribe][订阅主题: {}]", topic);

                        client.subscribe(topic, MqttQoS.AT_LEAST_ONCE.value())
                                .onComplete(subscribeAr -> {
                                    if (subscribeAr.succeeded()) {
                                        log.info("[testSubscribe][订阅成功，等待下行消息... (按 Ctrl+C 结束)]");
                                        // 保持连接 30 秒等待消息
                                        vertx.setTimer(30000, id -> {
                                            client.disconnect()
                                                    .onComplete(disconnectAr -> {
                                                        log.info("[testSubscribe][断开连接]");
                                                        latch.countDown();
                                                    });
                                        });
                                    } else {
                                        log.error("[testSubscribe][订阅失败]", subscribeAr.cause());
                                        latch.countDown();
                                    }
                                });
                    } else {
                        log.error("[testSubscribe][连接失败]", connectAr.cause());
                        latch.countDown();
                    }
                });

        // 6. 等待测试完成
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("[testSubscribe][测试超时]");
        }
    }

}
