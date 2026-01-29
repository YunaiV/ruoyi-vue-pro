package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
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
 * IoT 网关子设备 MQTT 协议集成测试（手动测试）
 *
 * <p>测试场景：子设备（IotProductDeviceTypeEnum 的 SUB 类型）通过网关设备代理上报数据
 *
 * <p><b>重要说明：子设备无法直接连接平台，所有请求均由网关设备（Gateway）代为转发。</b>
 * <p>网关设备转发子设备请求时，使用子设备自己的认证信息连接。
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（MQTT 端口 1883）</li>
 *     <li>确保子设备已通过 {@link IotGatewayDeviceMqttProtocolIntegrationTest#testTopoAdd()} 绑定到网关</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testAuth()} - 子设备连接认证</li>
 *             <li>{@link #testPropertyPost()} - 子设备属性上报（由网关代理转发）</li>
 *             <li>{@link #testEventPost()} - 子设备事件上报（由网关代理转发）</li>
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
public class IotGatewaySubDeviceMqttProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 1883;
    private static final int TIMEOUT_SECONDS = 10;

    private static Vertx vertx;

    // ===================== 编解码器（MQTT 使用 Alink 协议） =====================

    private static final IotDeviceMessageCodec CODEC = new IotAlinkDeviceMessageCodec();

    // ===================== 网关子设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================

    private static final String PRODUCT_KEY = "jAufEMTF1W6wnPhn";
    private static final String DEVICE_NAME = "chazuo-it";
    private static final String DEVICE_SECRET = "d46ef9b28ab14238b9c00a3a668032af";

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
     * 子设备认证测试：获取子设备 Token
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

    // ===================== 子设备属性上报测试 =====================

    /**
     * 子设备属性上报测试
     */
    @Test
    public void testPropertyPost() throws Exception {
        // 1. 连接并认证
        MqttClient client = connectAndAuth();
        log.info("[testPropertyPost][连接认证成功]");
        log.info("[testPropertyPost][子设备属性上报 - 请求实际由 Gateway 代为转发]");

        // 2. 订阅 _reply 主题
        String replyTopic = String.format("/sys/%s/%s/thing/property/post_reply", PRODUCT_KEY, DEVICE_NAME);
        subscribeReply(client, replyTopic);

        // 3. 构建属性上报消息
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                        .put("power", 100)
                        .put("status", "online")
                        .put("temperature", 36.5)
                        .build()),
                null, null, null);

        // 4. 发布消息并等待响应
        String topic = String.format("/sys/%s/%s/thing/property/post", PRODUCT_KEY, DEVICE_NAME);
        IotDeviceMessage response = publishAndWaitReply(client, topic, request);
        log.info("[testPropertyPost][响应消息: {}]", response);

        // 5. 断开连接
        disconnect(client);
    }

    // ===================== 子设备事件上报测试 =====================

    /**
     * 子设备事件上报测试
     */
    @Test
    public void testEventPost() throws Exception {
        // 1. 连接并认证
        MqttClient client = connectAndAuth();
        log.info("[testEventPost][连接认证成功]");
        log.info("[testEventPost][子设备事件上报 - 请求实际由 Gateway 代为转发]");

        // 2. 订阅 _reply 主题
        String replyTopic = String.format("/sys/%s/%s/thing/event/post_reply", PRODUCT_KEY, DEVICE_NAME);
        subscribeReply(client, replyTopic);

        // 3. 构建事件上报消息
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                IotDeviceEventPostReqDTO.of(
                        "alarm",
                        MapUtil.<String, Object>builder()
                                .put("level", "warning")
                                .put("message", "temperature too high")
                                .put("threshold", 40)
                                .put("current", 42)
                                .build(),
                        System.currentTimeMillis()),
                null, null, null);

        // 4. 发布消息并等待响应
        String topic = String.format("/sys/%s/%s/thing/event/post", PRODUCT_KEY, DEVICE_NAME);
        IotDeviceMessage response = publishAndWaitReply(client, topic, request);
        log.info("[testEventPost][响应消息: {}]", response);

        // 5. 断开连接
        disconnect(client);
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
     * 连接并认证子设备
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
