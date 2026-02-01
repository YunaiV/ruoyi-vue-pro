package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.codec.alink.IotAlinkDeviceMessageCodec;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
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
 *             <li>{@link #testAuth()} - 设备连接认证</li>
 *             <li>{@link #testPropertyPost()} - 设备属性上报</li>
 *             <li>{@link #testEventPost()} - 设备事件上报</li>
 *             <li>{@link #testSubscribe()} - 订阅下行消息</li>
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
@Disabled
public class IotDirectDeviceMqttProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 1883;
    private static final int TIMEOUT_SECONDS = 10;

    private static Vertx vertx;

    // ===================== 编解码器（MQTT 使用 Alink 协议） =====================

    private static final IotDeviceMessageCodec CODEC = new IotAlinkDeviceMessageCodec();

    // ===================== 直连设备信息（根据实际情况修改，从 iot_device 表查询） =====================

    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String DEVICE_SECRET = "0baa4c2ecc104ae1a26b4070c218bdf3";

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
     * 认证测试：获取设备 Token
     */
    @Test
    public void testAuth() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        // 1. 构建认证信息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        log.info("[testAuth][认证信息: clientId={}, username={}, password={}]",
                authInfo.getClientId(), authInfo.getUsername(), authInfo.getPassword());

        // 2. 创建客户端并连接
        MqttClient client = connect(authInfo);
        client.connect(SERVER_PORT, SERVER_HOST)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        log.info("[testAuth][连接成功，客户端 ID: {}]", client.clientId());
                        // 断开连接
                        client.disconnect()
                                .onComplete(disconnectAr -> {
                                    if (disconnectAr.succeeded()) {
                                        log.info("[testAuth][断开连接成功]");
                                    } else {
                                        log.error("[testAuth][断开连接失败]", disconnectAr.cause());
                                    }
                                    latch.countDown();
                                });
                    } else {
                        log.error("[testAuth][连接失败]", ar.cause());
                        latch.countDown();
                    }
                });

        // 3. 等待测试完成
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("[testAuth][测试超时]");
        }
    }

    // ===================== 直连设备属性上报测试 =====================

    /**
     * 属性上报测试
     */
    @Test
    public void testPropertyPost() throws Exception {
        // 1. 连接并认证
        MqttClient client = connectAndAuth();
        log.info("[testPropertyPost][连接认证成功]");

        // 2. 订阅 _reply 主题
        String replyTopic = String.format("/sys/%s/%s/thing/property/post_reply", PRODUCT_KEY, DEVICE_NAME);
        subscribeReply(client, replyTopic);

        // 3. 构建属性上报消息
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                        .put("width", 1)
                        .put("height", "2")
                        .build()),
                null, null, null);

        // 4. 发布消息并等待响应
        String topic = String.format("/sys/%s/%s/thing/property/post", PRODUCT_KEY, DEVICE_NAME);
        IotDeviceMessage response = publishAndWaitReply(client, topic, request);
        log.info("[testPropertyPost][响应消息: {}]", response);

        // 5. 断开连接
        disconnect(client);
    }

    // ===================== 直连设备事件上报测试 =====================

    /**
     * 事件上报测试
     */
    @Test
    public void testEventPost() throws Exception {
        // 1. 连接并认证
        MqttClient client = connectAndAuth();
        log.info("[testEventPost][连接认证成功]");

        // 2. 订阅 _reply 主题
        String replyTopic = String.format("/sys/%s/%s/thing/event/post_reply", PRODUCT_KEY, DEVICE_NAME);
        subscribeReply(client, replyTopic);

        // 3. 构建事件上报消息
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                IotDeviceEventPostReqDTO.of(
                        "eat",
                        MapUtil.<String, Object>builder().put("rice", 3).build(),
                        System.currentTimeMillis()),
                null, null, null);

        // 4. 发布消息并等待响应
        String topic = String.format("/sys/%s/%s/thing/event/post", PRODUCT_KEY, DEVICE_NAME);
        IotDeviceMessage response = publishAndWaitReply(client, topic, request);
        log.info("[testEventPost][响应消息: {}]", response);

        // 5. 断开连接
        disconnect(client);
    }

    // ===================== 设备动态注册测试（一型一密） =====================

    /**
     * 直连设备动态注册测试（一型一密）
     * <p>
     * 使用产品密钥（productSecret）验证身份，成功后返回设备密钥（deviceSecret）
     * <p>
     * 注意：此接口不需要认证
     */
    @Test
    public void testDeviceRegister() throws Exception {
        // 1. 连接并认证（使用已有设备连接）
        MqttClient client = connectAndAuth();
        log.info("[testDeviceRegister][连接认证成功]");

        // 2.1 构建注册消息
        IotDeviceRegisterReqDTO registerReqDTO = new IotDeviceRegisterReqDTO();
        registerReqDTO.setProductKey(PRODUCT_KEY);
        registerReqDTO.setDeviceName("test-mqtt-" + System.currentTimeMillis());
        registerReqDTO.setProductSecret("test-product-secret");
        IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod(), registerReqDTO, null, null, null);
        // 2.2 订阅 _reply 主题
        String replyTopic = String.format("/sys/%s/%s/thing/auth/register_reply",
                registerReqDTO.getProductKey(), registerReqDTO.getDeviceName());
        subscribeReply(client, replyTopic);

        // 3. 发布消息并等待响应
        String topic = String.format("/sys/%s/%s/thing/auth/register",
                registerReqDTO.getProductKey(), registerReqDTO.getDeviceName());
        IotDeviceMessage response = publishAndWaitReply(client, topic, request);
        log.info("[testDeviceRegister][响应消息: {}]", response);
        log.info("[testDeviceRegister][成功后可使用返回的 deviceSecret 进行一机一密认证]");

        // 4. 断开连接
        disconnect(client);
    }

    // ===================== 订阅下行消息测试 =====================

    /**
     * 订阅下行消息测试：订阅服务端下发的消息
     */
    @Test
    public void testSubscribe() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        // 1. 连接并认证
        MqttClient client = connectAndAuth();
        log.info("[testSubscribe][连接认证成功]");

        // 2. 设置消息处理器
        client.publishHandler(message -> {
            log.info("[testSubscribe][收到消息: topic={}, payload={}]",
                    message.topicName(), message.payload().toString());
        });

        // 3. 订阅下行主题
        String topic = String.format("/sys/%s/%s/thing/service/#", PRODUCT_KEY, DEVICE_NAME);
        log.info("[testSubscribe][订阅主题: {}]", topic);

        client.subscribe(topic, MqttQoS.AT_LEAST_ONCE.value())
                .onComplete(subscribeAr -> {
                    if (subscribeAr.succeeded()) {
                        log.info("[testSubscribe][订阅成功，等待下行消息... (30秒后自动断开)]");
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

        // 4. 等待测试完成
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("[testSubscribe][测试超时]");
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 创建 MQTT 客户端
     *
     * @param authInfo 认证信息
     * @return MQTT 客户端
     */
    private MqttClient connect(IotDeviceAuthReqDTO authInfo) {
        MqttClientOptions options = new MqttClientOptions()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword())
                .setCleanSession(true)
                .setKeepAliveInterval(60);
        return MqttClient.create(vertx, options);
    }

    /**
     * 连接并认证设备
     *
     * @return 已认证的 MQTT 客户端
     */
    private MqttClient connectAndAuth() throws Exception {
        // 1. 创建客户端并连接
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        MqttClient client = connect(authInfo);

        // 2.1 连接
        CompletableFuture<MqttClient> future = new CompletableFuture<>();
        client.connect(SERVER_PORT, SERVER_HOST)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        future.complete(client);
                    } else {
                        future.completeExceptionally(ar.cause());
                    }
                });
        // 2.2 等待连接结果
        return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 订阅响应主题
     *
     * @param client     MQTT 客户端
     * @param replyTopic 响应主题
     */
    private void subscribeReply(MqttClient client, String replyTopic) throws Exception {
        // 1. 订阅响应主题
        CompletableFuture<Void> future = new CompletableFuture<>();
        client.subscribe(replyTopic, MqttQoS.AT_LEAST_ONCE.value())
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        log.info("[subscribeReply][订阅响应主题成功: {}]", replyTopic);
                        future.complete(null);
                    } else {
                        future.completeExceptionally(ar.cause());
                    }
                });
        // 2. 等待订阅结果
        future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 发布消息并等待响应
     *
     * @param client  MQTT 客户端
     * @param topic   发布主题
     * @param request 请求消息
     * @return 响应消息
     */
    private IotDeviceMessage publishAndWaitReply(MqttClient client, String topic, IotDeviceMessage request) {
        // 1. 设置消息处理器，接收响应
        CompletableFuture<IotDeviceMessage> future = new CompletableFuture<>();
        client.publishHandler(message -> {
            log.info("[publishAndWaitReply][收到响应: topic={}, payload={}]",
                    message.topicName(), message.payload().toString());
            IotDeviceMessage response = CODEC.decode(message.payload().getBytes());
            future.complete(response);
        });

        // 2. 编码并发布消息
        byte[] payload = CODEC.encode(request);
        log.info("[publishAndWaitReply][Codec: {}, 发送消息: topic={}, payload={}]",
                CODEC.type(), topic, new String(payload));

        client.publish(topic, Buffer.buffer(payload), MqttQoS.AT_LEAST_ONCE, false, false)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        log.info("[publishAndWaitReply][消息发布成功，messageId={}]", ar.result());
                    } else {
                        log.error("[publishAndWaitReply][消息发布失败]", ar.cause());
                        future.completeExceptionally(ar.cause());
                    }
                });

        // 3. 等待响应（超时返回 null）
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[publishAndWaitReply][等待响应超时或失败]");
            return null;
        }
    }

    /**
     * 断开连接
     *
     * @param client MQTT 客户端
     */
    private void disconnect(MqttClient client) throws Exception {
        // 1. 断开连接
        CompletableFuture<Void> future = new CompletableFuture<>();
        client.disconnect()
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        log.info("[disconnect][断开连接成功]");
                        future.complete(null);
                    } else {
                        future.completeExceptionally(ar.cause());
                    }
                });
        // 2. 等待断开结果
        future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

}
