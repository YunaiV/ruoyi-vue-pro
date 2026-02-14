package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.core.util.IotProductAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.json.IotJsonSerializer;
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

    // ===================== 序列化器 =====================

    private static final IotMessageSerializer SERIALIZER = new IotJsonSerializer();

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
        // 1. 构建认证信息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        log.info("[testAuth][认证信息: clientId={}, username={}, password={}]",
                authInfo.getClientId(), authInfo.getUsername(), authInfo.getPassword());

        // 2. 创建客户端并连接
        MqttClient client = createClient(authInfo);
        try {
            client.connect(SERVER_PORT, SERVER_HOST)
                    .toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("[testAuth][连接成功，客户端 ID: {}]", client.clientId());
        } finally {
            disconnect(client);
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

        try {
            // 2.1 构建属性上报消息
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                    IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                            .put("width", 1)
                            .put("height", "2")
                            .build()));

            // 2.2 订阅 _reply 主题
            String replyTopic = String.format("/sys/%s/%s/thing/property/post_reply", PRODUCT_KEY, DEVICE_NAME);
            subscribe(client, replyTopic);

            // 2.2 发布消息并等待响应
            String topic = String.format("/sys/%s/%s/thing/property/post", PRODUCT_KEY, DEVICE_NAME);
            IotDeviceMessage response = publishAndWaitReply(client, topic, request);
            log.info("[testPropertyPost][响应消息: {}]", response);
        } finally {
            disconnect(client);
        }
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

        try {
            // 2.1 构建事件上报消息
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                    IotDeviceEventPostReqDTO.of(
                            "eat",
                            MapUtil.<String, Object>builder().put("rice", 3).build(),
                            System.currentTimeMillis()));

            // 2.2 订阅 _reply 主题
            String replyTopic = String.format("/sys/%s/%s/thing/event/post_reply", PRODUCT_KEY, DEVICE_NAME);
            subscribe(client, replyTopic);

            // 3. 发布消息并等待响应
            String topic = String.format("/sys/%s/%s/thing/event/post", PRODUCT_KEY, DEVICE_NAME);
            IotDeviceMessage response = publishAndWaitReply(client, topic, request);
            log.info("[testEventPost][响应消息: {}]", response);
        } finally {
            disconnect(client);
        }
    }

    // ===================== 设备动态注册测试（一型一密） =====================

    /**
     * 直连设备动态注册测试（一型一密）
     * <p>
     * 认证方式：
     * - clientId: 任意值 + "|authType=register|" 后缀
     * - username: {deviceName}&{productKey}（与普通认证相同）
     * - password: 签名（使用 productSecret 对 "deviceName" + deviceName + "productKey" + productKey 进行 HMAC-SHA256）
     * <p>
     * 成功后返回设备密钥（deviceSecret），可用于后续一机一密认证
     */
    @Test
    public void testDeviceRegister() throws Exception {
        // 1.1 构建注册参数
        String deviceName = "test-mqtt-" + System.currentTimeMillis();
        String productSecret = "test-product-secret"; // 替换为实际的 productSecret
        String sign = IotProductAuthUtils.buildSign(PRODUCT_KEY, deviceName, productSecret);
        // 1.2 构建 MQTT 连接参数（clientId 需要添加 |authType=register| 后缀）
        String clientId = IotDeviceAuthUtils.buildClientId(PRODUCT_KEY, deviceName) + "|authType=register|";
        String username = IotDeviceAuthUtils.buildUsername(PRODUCT_KEY, deviceName);
        log.info("[testDeviceRegister][注册参数: clientId={}, username={}, sign={}]",
                clientId, username, sign);
        // 1.3 创建客户端并连接（连接时服务端自动处理注册）
        MqttClientOptions options = new MqttClientOptions()
                .setClientId(clientId)
                .setUsername(username)
                .setPassword(sign)
                .setCleanSession(true)
                .setKeepAliveInterval(60);
        MqttClient client = MqttClient.create(vertx, options);

        try {
            // 2. 连接服务器（连接成功后服务端会自动处理注册并发送响应）
            client.connect(SERVER_PORT, SERVER_HOST)
                    .toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("[testDeviceRegister][连接成功，等待注册响应...]");

            // 3.1 设置消息处理器，接收注册响应
            CompletableFuture<IotDeviceMessage> responseFuture = new CompletableFuture<>();
            client.publishHandler(message -> {
                log.info("[testDeviceRegister][收到响应: topic={}, payload={}]",
                        message.topicName(), message.payload().toString());
                IotDeviceMessage response = SERIALIZER.deserialize(message.payload().getBytes());
                responseFuture.complete(response);
            });
            // 3.2 订阅 _reply 主题
            String replyTopic = String.format("/sys/%s/%s/thing/auth/register_reply", PRODUCT_KEY, deviceName);
            subscribe(client, replyTopic);

            // 4. 等待注册响应
            IotDeviceMessage response = responseFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("[testDeviceRegister][注册响应: {}]", response);
            log.info("[testDeviceRegister][成功后可使用返回的 deviceSecret 进行一机一密认证]");
        } finally {
            disconnect(client);
        }
    }

    // ===================== 订阅下行消息测试 =====================

    /**
     * 订阅下行消息测试：订阅服务端下发的消息
     */
    @Test
    public void testSubscribe() throws Exception {
        // 1. 连接并认证
        MqttClient client = connectAndAuth();
        log.info("[testSubscribe][连接认证成功]");

        try {
            // 2. 设置消息处理器：收到属性设置时，回复 _reply 消息
            client.publishHandler(message -> {
                log.info("[testSubscribe][收到消息: topic={}, payload={}]",
                        message.topicName(), message.payload().toString());
                // 收到属性设置消息时，回复 _reply
                if (message.topicName().endsWith("/thing/property/set")) {
                    try {
                        IotDeviceMessage received = SERIALIZER.deserialize(message.payload().getBytes());
                        IotDeviceMessage reply = IotDeviceMessage.replyOf(
                                received.getRequestId(), "thing.property.set_reply", null, 0, null);
                        String replyTopic = String.format("/sys/%s/%s/thing/property/set_reply", PRODUCT_KEY, DEVICE_NAME);
                        byte[] replyPayload = SERIALIZER.serialize(reply);
                        client.publish(replyTopic, Buffer.buffer(replyPayload), MqttQoS.AT_LEAST_ONCE, false, false);
                        log.info("[testSubscribe][已回复属性设置: topic={}]", replyTopic);
                    } catch (Exception e) {
                        log.error("[testSubscribe][回复属性设置异常]", e);
                    }
                }
            });

            // 3. 订阅下行主题（属性设置 + 服务调用）
            String topic = String.format("/sys/%s/%s/#", PRODUCT_KEY, DEVICE_NAME);
            log.info("[testSubscribe][订阅主题: {}]", topic);
            subscribe(client, topic);
            log.info("[testSubscribe][订阅成功，等待下行消息... (30秒后自动断开)]");

            // 4. 保持连接 30 秒等待消息
            Thread.sleep(30000);
        } finally {
            disconnect(client);
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 创建 MQTT 客户端
     *
     * @param authInfo 认证信息
     * @return MQTT 客户端
     */
    private MqttClient createClient(IotDeviceAuthReqDTO authInfo) {
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
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        MqttClient client = createClient(authInfo);
        client.connect(SERVER_PORT, SERVER_HOST)
                .toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return client;
    }

    /**
     * 订阅主题
     *
     * @param client MQTT 客户端
     * @param topic  主题
     */
    private void subscribe(MqttClient client, String topic) throws Exception {
        client.subscribe(topic, MqttQoS.AT_LEAST_ONCE.value())
                .toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        log.info("[subscribe][订阅主题成功: {}]", topic);
    }

    /**
     * 发布消息并等待响应
     *
     * @param client  MQTT 客户端
     * @param topic   发布主题
     * @param request 请求消息
     * @return 响应消息
     */
    private IotDeviceMessage publishAndWaitReply(MqttClient client, String topic, IotDeviceMessage request)
            throws Exception {
        // 1. 设置消息处理器，接收响应
        CompletableFuture<IotDeviceMessage> responseFuture = new CompletableFuture<>();
        client.publishHandler(message -> {
            log.info("[publishAndWaitReply][收到响应: topic={}, payload={}]",
                    message.topicName(), message.payload().toString());
            IotDeviceMessage response = SERIALIZER.deserialize(message.payload().getBytes());
            responseFuture.complete(response);
        });

        // 2. 序列化并发布消息
        byte[] payload = SERIALIZER.serialize(request);
        log.info("[publishAndWaitReply][Serializer: {}, 发送消息: topic={}, payload={}]",
                SERIALIZER.getType(), topic, new String(payload));
        client.publish(topic, Buffer.buffer(payload), MqttQoS.AT_LEAST_ONCE, false, false)
                .toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        log.info("[publishAndWaitReply][消息发布成功]");

        // 3. 等待响应
        try {
            return responseFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
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
        client.disconnect()
                .toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        log.info("[disconnect][断开连接成功]");
    }

}
