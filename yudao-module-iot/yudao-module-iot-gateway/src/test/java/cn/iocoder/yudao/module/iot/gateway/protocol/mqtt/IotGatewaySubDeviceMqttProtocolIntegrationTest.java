package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
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

    // ===================== 序列化器 =====================

    private static final IotMessageSerializer SERIALIZER = new IotJsonSerializer();

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

        try {
            // 2.1 构建属性上报消息
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                    IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                            .put("power", 100)
                            .put("status", "online")
                            .put("temperature", 36.5)
                            .build()));

            // 2.2 订阅 _reply 主题
            String replyTopic = String.format("/sys/%s/%s/thing/property/post_reply", PRODUCT_KEY, DEVICE_NAME);
            subscribe(client, replyTopic);

            // 3. 发布消息并等待响应
            String topic = String.format("/sys/%s/%s/thing/property/post", PRODUCT_KEY, DEVICE_NAME);
            IotDeviceMessage response = publishAndWaitReply(client, topic, request);
            log.info("[testPropertyPost][响应消息: {}]", response);
        } finally {
            disconnect(client);
        }
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

        try {
            // 2.1 构建事件上报消息
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                    IotDeviceEventPostReqDTO.of(
                            "alarm",
                            MapUtil.<String, Object>builder()
                                    .put("level", "warning")
                                    .put("message", "temperature too high")
                                    .put("threshold", 40)
                                    .put("current", 42)
                                    .build(),
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
     * 连接并认证子设备
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
