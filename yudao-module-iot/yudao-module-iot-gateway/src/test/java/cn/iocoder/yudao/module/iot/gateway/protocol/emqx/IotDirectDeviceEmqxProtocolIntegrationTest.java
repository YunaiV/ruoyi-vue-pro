package cn.iocoder.yudao.module.iot.gateway.protocol.emqx;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
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

import java.util.concurrent.TimeUnit;

/**
 * IoT 直连设备 EMQX 协议集成测试（手动测试）
 *
 * <p>测试场景：直连设备（IotProductDeviceTypeEnum 的 DIRECT 类型）通过 EMQX Broker 连接平台
 *
 * <p>EMQX 协议架构：
 * <pre>
 *     +--------+       MQTT        +-------------+       HTTP Hook        +---------+
 *     | 设备   | ----------------> | EMQX Broker | --------------------> | 网关    |
 *     +--------+                   +-------------+                        +---------+
 *         |                              |                                     |
 *         | 1. 连接认证                   | 2. 调用 /mqtt/auth                   |
 *         | 3. 发布消息                   | 4. 调用 /mqtt/event (上线/下线)       |
 *         |                              | 5. 网关订阅 EMQX 消息                 |
 *         |                              |                                     |
 * </pre>
 *
 * <p>测试分类：
 * <ul>
 *     <li>第一部分：模拟设备连接 EMQX Broker，发送 MQTT 消息</li>
 *     <li>第二部分：模拟 EMQX Server 调用网关 HTTP Hook 接口（认证、事件）</li>
 * </ul>
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 EMQX Broker（MQTT 端口 1883）</li>
 *     <li>启动 yudao-module-iot-gateway 服务（HTTP 端口 18083）</li>
 *     <li>配置 EMQX HTTP 认证插件指向网关的 /mqtt/auth 接口</li>
 *     <li>配置 EMQX Webhook 插件指向网关的 /mqtt/event 接口</li>
 *     <li>运行测试方法</li>
 * </ol>
 *
 * @author 芋道源码
 */
@Slf4j
@Disabled
@SuppressWarnings("HttpUrlsUsage")
public class IotDirectDeviceEmqxProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    /**
     * EMQX 认证事件 HTTP 接口端口（网关提供给 EMQX Server 调用）
     */
    private static final int HTTP_PORT = 18083;
    /**
     * EMQX Broker MQTT 端口（设备连接 EMQX）
     */
    private static final int MQTT_PORT = 1883;
    private static final int TIMEOUT_SECONDS = 10;

    private static Vertx vertx;

    // ===================== 编解码器（EMQX 使用 Alink 协议） =====================

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

    // ==================================================================================
    // 第一部分：模拟设备连接 EMQX Broker
    // ==================================================================================

    /**
     * 设备连接测试：模拟设备连接 EMQX Broker
     * <p>
     * 当设备连接 EMQX 时，EMQX 会自动调用网关的 /mqtt/auth 接口进行认证
     */
    @Test
    public void testDeviceConnect() throws Exception {
        // 1. 构建认证信息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        log.info("[testDeviceConnect][认证信息: clientId={}, username={}, password={}]",
                authInfo.getClientId(), authInfo.getUsername(), authInfo.getPassword());

        // 2. 创建客户端并连接 EMQX Broker
        MqttClient client = createClient(authInfo);
        try {
            client.connect(MQTT_PORT, SERVER_HOST)
                    .toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("[testDeviceConnect][连接 EMQX Broker 成功，客户端 ID: {}]", client.clientId());
            log.info("[testDeviceConnect][EMQX 会自动调用网关的 /mqtt/auth 接口进行认证]");
            log.info("[testDeviceConnect][EMQX 会自动调用网关的 /mqtt/event 接口通知设备上线]");
        } finally {
            disconnect(client);
            log.info("[testDeviceConnect][EMQX 会自动调用网关的 /mqtt/event 接口通知设备下线]");
        }
    }

    /**
     * 属性上报测试：设备通过 EMQX Broker 发布属性消息
     * <p>
     * 消息流程：设备 -> EMQX Broker -> 网关（订阅 EMQX 消息）
     */
    @Test
    public void testPropertyPost() throws Exception {
        // 1. 连接 EMQX Broker
        MqttClient client = connectToEmqx();
        log.info("[testPropertyPost][连接 EMQX Broker 成功]");

        try {
            // 2.1 构建属性上报消息
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                    IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                            .put("width", 1)
                            .put("height", "2")
                            .build()));

            // 2.2 发布消息到 EMQX Broker
            String topic = String.format("/sys/%s/%s/thing/property/post", PRODUCT_KEY, DEVICE_NAME);
            publish(client, topic, request);
            log.info("[testPropertyPost][属性上报消息已发送到 EMQX Broker]");
            log.info("[testPropertyPost][网关会通过订阅 EMQX 接收此消息]");

            // 2.3 等待消息处理
            Thread.sleep(2000);
            log.info("[testPropertyPost][请检查网关日志确认消息是否被正确处理]");
        } finally {
            disconnect(client);
        }
    }

    /**
     * 事件上报测试：设备通过 EMQX Broker 发布事件消息
     * <p>
     * 消息流程：设备 -> EMQX Broker -> 网关（订阅 EMQX 消息）
     */
    @Test
    public void testEventPost() throws Exception {
        // 1. 连接 EMQX Broker
        MqttClient client = connectToEmqx();
        log.info("[testEventPost][连接 EMQX Broker 成功]");

        try {
            // 2.1 构建事件上报消息
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                    IotDeviceEventPostReqDTO.of(
                            "eat",
                            MapUtil.<String, Object>builder().put("rice", 3).build(),
                            System.currentTimeMillis()));

            // 2.2 发布消息到 EMQX Broker
            String topic = String.format("/sys/%s/%s/thing/event/post", PRODUCT_KEY, DEVICE_NAME);
            publish(client, topic, request);
            log.info("[testEventPost][事件上报消息已发送到 EMQX Broker]");
            log.info("[testEventPost][网关会通过订阅 EMQX 接收此消息]");

            // 2.3 等待消息处理
            Thread.sleep(2000);
            log.info("[testEventPost][请检查网关日志确认消息是否被正确处理]");
        } finally {
            disconnect(client);
        }
    }

    /**
     * 订阅下行消息测试：设备订阅服务端下发的消息
     * <p>
     * 消息流程：网关 -> EMQX Broker -> 设备
     */
    @Test
    public void testSubscribe() throws Exception {
        // 1. 连接 EMQX Broker
        MqttClient client = connectToEmqx();
        log.info("[testSubscribe][连接 EMQX Broker 成功]");

        try {
            // 2. 设置消息处理器
            client.publishHandler(message -> log.info("[testSubscribe][收到下行消息: topic={}, payload={}]",
                    message.topicName(), message.payload().toString()));

            // 3. 订阅下行主题
            String topic = String.format("/sys/%s/%s/thing/service/#", PRODUCT_KEY, DEVICE_NAME);
            log.info("[testSubscribe][订阅主题: {}]", topic);
            subscribe(client, topic);
            log.info("[testSubscribe][订阅成功，等待下行消息... (30秒后自动断开)]");
            log.info("[testSubscribe][网关下发的消息会通过 EMQX Broker 转发给设备]");

            // 4. 保持连接 30 秒等待消息
            Thread.sleep(30000);
        } finally {
            disconnect(client);
        }
    }

    // ==================================================================================
    // 第二部分：模拟 EMQX Server 调用网关 HTTP Hook 接口
    // 说明：这些接口是 EMQX Server 自动调用的，这里只是用于单独测试接口功能
    // ==================================================================================

    /**
     * 认证接口测试：模拟 EMQX Server 调用 /mqtt/auth 接口
     * <p>
     * 注意：正常情况下此接口由 EMQX HTTP 认证插件自动调用，这里只是测试接口本身
     */
    @Test
    public void testEmqxAuthHook() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/mqtt/auth", SERVER_HOST, HTTP_PORT);
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        // 1.2 EMQX 认证请求格式
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("clientid", authInfo.getClientId())
                .put("username", authInfo.getUsername())
                .put("password", authInfo.getPassword())
                .build());
        // 1.3 输出请求
        log.info("[testEmqxAuthHook][模拟 EMQX Server 调用认证接口]");
        log.info("[testEmqxAuthHook][请求 URL: {}]", url);
        log.info("[testEmqxAuthHook][请求体: {}]", payload);

        // 2.1 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testEmqxAuthHook][响应状态码: {}]", httpResponse.getStatus());
            log.info("[testEmqxAuthHook][响应体: {}]", httpResponse.body());
            log.info("[testEmqxAuthHook][认证结果: result=allow 表示认证成功, result=deny 表示认证失败]");
        }
    }

    /**
     * 认证失败测试：模拟 EMQX Server 调用 /mqtt/auth 接口（错误密码）
     */
    @Test
    public void testEmqxAuthHookFailed() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/mqtt/auth", SERVER_HOST, HTTP_PORT);
        // 1.2 使用错误的密码
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("clientid", PRODUCT_KEY + "." + DEVICE_NAME)
                .put("username", DEVICE_NAME + "&" + PRODUCT_KEY)
                .put("password", "wrong_password")
                .build());
        // 1.3 输出请求
        log.info("[testEmqxAuthHookFailed][模拟 EMQX Server 调用认证接口（错误密码）]");
        log.info("[testEmqxAuthHookFailed][请求 URL: {}]", url);
        log.info("[testEmqxAuthHookFailed][请求体: {}]", payload);

        // 2.1 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testEmqxAuthHookFailed][响应状态码: {}]", httpResponse.getStatus());
            log.info("[testEmqxAuthHookFailed][响应体: {}]", httpResponse.body());
            log.info("[testEmqxAuthHookFailed][预期结果: result=deny]");
        }
    }

    /**
     * 设备上线事件测试：模拟 EMQX Server Webhook 调用 /mqtt/event 接口
     * <p>
     * 注意：正常情况下此接口由 EMQX Webhook 插件自动调用，这里只是测试接口本身
     */
    @Test
    public void testEmqxClientConnectedHook() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/mqtt/event", SERVER_HOST, HTTP_PORT);
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        // 1.2 EMQX Webhook client.connected 事件格式
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("event", "client.connected")
                .put("clientid", authInfo.getClientId())
                .put("username", authInfo.getUsername())
                .put("peername", "127.0.0.1:12345")
                .put("connected_at", System.currentTimeMillis())
                .build());
        // 1.3 输出请求
        log.info("[testEmqxClientConnectedHook][模拟 EMQX Server Webhook 调用设备上线事件]");
        log.info("[testEmqxClientConnectedHook][请求 URL: {}]", url);
        log.info("[testEmqxClientConnectedHook][请求体: {}]", payload);

        // 2.1 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testEmqxClientConnectedHook][响应状态码: {}]", httpResponse.getStatus());
            log.info("[testEmqxClientConnectedHook][响应体: {}]", httpResponse.body());
            log.info("[testEmqxClientConnectedHook][预期结果: 状态码 200，设备状态更新为在线]");
        }
    }

    /**
     * 设备下线事件测试：模拟 EMQX Server Webhook 调用 /mqtt/event 接口
     * <p>
     * 注意：正常情况下此接口由 EMQX Webhook 插件自动调用，这里只是测试接口本身
     */
    @Test
    public void testEmqxClientDisconnectedHook() {
        // 1.1 构建请求
        String url = String.format("http://%s:%d/mqtt/event", SERVER_HOST, HTTP_PORT);
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        // 1.2 EMQX Webhook client.disconnected 事件格式
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("event", "client.disconnected")
                .put("clientid", authInfo.getClientId())
                .put("username", authInfo.getUsername())
                .put("reason", "normal")
                .put("disconnected_at", System.currentTimeMillis())
                .build());
        // 1.3 输出请求
        log.info("[testEmqxClientDisconnectedHook][模拟 EMQX Server Webhook 调用设备下线事件]");
        log.info("[testEmqxClientDisconnectedHook][请求 URL: {}]", url);
        log.info("[testEmqxClientDisconnectedHook][请求体: {}]", payload);

        // 2.1 发送请求
        try (HttpResponse httpResponse = HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .body(payload)
                .execute()) {
            // 2.2 输出结果
            log.info("[testEmqxClientDisconnectedHook][响应状态码: {}]", httpResponse.getStatus());
            log.info("[testEmqxClientDisconnectedHook][响应体: {}]", httpResponse.body());
            log.info("[testEmqxClientDisconnectedHook][预期结果: 状态码 200，设备状态更新为离线]");
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
     * 连接 EMQX Broker 并认证设备
     *
     * @return 已认证的 MQTT 客户端
     */
    private MqttClient connectToEmqx() throws Exception {
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        MqttClient client = createClient(authInfo);
        client.connect(MQTT_PORT, SERVER_HOST)
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
     * 发布消息
     *
     * @param client  MQTT 客户端
     * @param topic   发布主题
     * @param request 请求消息
     */
    private void publish(MqttClient client, String topic, IotDeviceMessage request) throws Exception {
        byte[] payload = CODEC.encode(request);
        log.info("[publish][发送消息: topic={}, payload={}]", topic, new String(payload));
        client.publish(topic, Buffer.buffer(payload), MqttQoS.AT_LEAST_ONCE, false, false)
                .toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        log.info("[publish][消息发布成功]");
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
