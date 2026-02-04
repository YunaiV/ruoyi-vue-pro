package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.IotDeviceIdentity;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPackPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoAddReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoDeleteReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.topo.IotDeviceTopoGetReqDTO;
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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * IoT 网关设备 MQTT 协议集成测试（手动测试）
 *
 * <p>测试场景：网关设备（IotProductDeviceTypeEnum 的 GATEWAY 类型）通过 MQTT 协议管理子设备拓扑关系
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（MQTT 端口 1883）</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testAuth()} - 网关设备连接认证</li>
 *             <li>{@link #testTopoAdd()} - 添加子设备拓扑关系</li>
 *             <li>{@link #testTopoDelete()} - 删除子设备拓扑关系</li>
 *             <li>{@link #testTopoGet()} - 获取子设备拓扑关系</li>
 *             <li>{@link #testSubDeviceRegister()} - 子设备动态注册</li>
 *             <li>{@link #testPropertyPackPost()} - 批量上报属性（网关 + 子设备）</li>
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
public class IotGatewayDeviceMqttProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 1883;
    private static final int TIMEOUT_SECONDS = 10;

    private static Vertx vertx;

    // ===================== 序列化器 =====================

    private static final IotMessageSerializer SERIALIZER = new IotJsonSerializer();

    // ===================== 网关设备信息（根据实际情况修改，从 iot_device 表查询网关设备） =====================

    private static final String GATEWAY_PRODUCT_KEY = "m6XcS1ZJ3TW8eC0v";
    private static final String GATEWAY_DEVICE_NAME = "sub-ddd";
    private static final String GATEWAY_DEVICE_SECRET = "b3d62c70f8a4495487ed1d35d61ac2b3";

    // ===================== 子设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================

    private static final String SUB_DEVICE_PRODUCT_KEY = "jAufEMTF1W6wnPhn";
    private static final String SUB_DEVICE_NAME = "chazuo-it";
    private static final String SUB_DEVICE_SECRET = "d46ef9b28ab14238b9c00a3a668032af";

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
     * 网关设备认证测试：获取网关设备 Token
     */
    @Test
    public void testAuth() throws Exception {
        // 1. 构建认证信息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
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

    // ===================== 拓扑管理测试 =====================

    /**
     * 添加子设备拓扑关系测试
     * <p>
     * 网关设备向平台上报需要绑定的子设备信息
     */
    @Test
    public void testTopoAdd() throws Exception {
        // 1. 连接并认证
        MqttClient client = connectAndAuth();
        log.info("[testTopoAdd][连接认证成功]");

        try {
            // 2.1 构建子设备认证信息
            IotDeviceAuthReqDTO subAuthInfo = IotDeviceAuthUtils.getAuthInfo(
                    SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME, SUB_DEVICE_SECRET);
            IotDeviceAuthReqDTO subDeviceAuth = new IotDeviceAuthReqDTO()
                    .setClientId(subAuthInfo.getClientId())
                    .setUsername(subAuthInfo.getUsername())
                    .setPassword(subAuthInfo.getPassword());

            // 2.2 构建请求消息
            IotDeviceTopoAddReqDTO params = new IotDeviceTopoAddReqDTO()
                    .setSubDevices(Collections.singletonList(subDeviceAuth));
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.TOPO_ADD.getMethod(),
                    params);

            // 2.3 订阅 _reply 主题
            String replyTopic = String.format("/sys/%s/%s/thing/topo/add_reply",
                    GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
            subscribe(client, replyTopic);

            // 3. 发布消息并等待响应
            String topic = String.format("/sys/%s/%s/thing/topo/add",
                    GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
            IotDeviceMessage response = publishAndWaitReply(client, topic, request);
            log.info("[testTopoAdd][响应消息: {}]", response);
        } finally {
            disconnect(client);
        }
    }

    /**
     * 删除子设备拓扑关系测试
     * <p>
     * 网关设备向平台上报需要解绑的子设备信息
     */
    @Test
    public void testTopoDelete() throws Exception {
        // 1. 连接并认证
        MqttClient client = connectAndAuth();
        log.info("[testTopoDelete][连接认证成功]");

        try {
            // 2.1 构建请求消息
            IotDeviceTopoDeleteReqDTO params = new IotDeviceTopoDeleteReqDTO()
                    .setSubDevices(Collections.singletonList(
                            new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME)));
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.TOPO_DELETE.getMethod(),
                    params);

            // 2.2 订阅 _reply 主题
            String replyTopic = String.format("/sys/%s/%s/thing/topo/delete_reply",
                    GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
            subscribe(client, replyTopic);

            // 3. 发布消息并等待响应
            String topic = String.format("/sys/%s/%s/thing/topo/delete",
                    GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
            IotDeviceMessage response = publishAndWaitReply(client, topic, request);
            log.info("[testTopoDelete][响应消息: {}]", response);
        } finally {
            disconnect(client);
        }
    }

    /**
     * 获取子设备拓扑关系测试
     * <p>
     * 网关设备向平台查询已绑定的子设备列表
     */
    @Test
    public void testTopoGet() throws Exception {
        // 1. 连接并认证
        MqttClient client = connectAndAuth();
        log.info("[testTopoGet][连接认证成功]");

        try {
            // 2.1 构建请求消息
            IotDeviceTopoGetReqDTO params = new IotDeviceTopoGetReqDTO();
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.TOPO_GET.getMethod(),
                    params);

            // 2.2 订阅 _reply 主题
            String replyTopic = String.format("/sys/%s/%s/thing/topo/get_reply",
                    GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
            subscribe(client, replyTopic);

            // 3. 发布消息并等待响应
            String topic = String.format("/sys/%s/%s/thing/topo/get",
                    GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
            IotDeviceMessage response = publishAndWaitReply(client, topic, request);
            log.info("[testTopoGet][响应消息: {}]", response);
        } finally {
            disconnect(client);
        }
    }

    // ===================== 子设备注册测试 =====================

    /**
     * 子设备动态注册测试
     * <p>
     * 网关设备代理子设备进行动态注册，平台返回子设备的 deviceSecret
     * <p>
     * 注意：此接口需要网关认证
     */
    @Test
    public void testSubDeviceRegister() throws Exception {
        // 1. 连接并认证
        MqttClient client = connectAndAuth();
        log.info("[testSubDeviceRegister][连接认证成功]");

        try {
            // 2.1 构建请求消息
            IotSubDeviceRegisterReqDTO subDevice = new IotSubDeviceRegisterReqDTO()
                    .setProductKey(SUB_DEVICE_PRODUCT_KEY)
                    .setDeviceName("mougezishebei-mqtt");
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod(),
                    Collections.singletonList(subDevice));

            // 2.2 订阅 _reply 主题
            String replyTopic = String.format("/sys/%s/%s/thing/auth/sub-device/register_reply",
                    GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
            subscribe(client, replyTopic);

            // 3. 发布消息并等待响应
            String topic = String.format("/sys/%s/%s/thing/auth/sub-device/register",
                    GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
            IotDeviceMessage response = publishAndWaitReply(client, topic, request);
            log.info("[testSubDeviceRegister][响应消息: {}]", response);
        } finally {
            disconnect(client);
        }
    }

    // ===================== 批量上报测试 =====================

    /**
     * 批量上报属性测试（网关 + 子设备）
     * <p>
     * 网关设备批量上报自身属性、事件，以及子设备的属性、事件
     */
    @Test
    public void testPropertyPackPost() throws Exception {
        // 1. 连接并认证
        MqttClient client = connectAndAuth();
        log.info("[testPropertyPackPost][连接认证成功]");

        try {
            // 2.1 构建【网关设备】自身属性
            Map<String, Object> gatewayProperties = MapUtil.<String, Object>builder()
                    .put("temperature", 25.5)
                    .build();

            // 2.2 构建【网关设备】自身事件
            IotDevicePropertyPackPostReqDTO.EventValue gatewayEvent = new IotDevicePropertyPackPostReqDTO.EventValue()
                    .setValue(MapUtil.builder().put("message", "gateway started").build())
                    .setTime(System.currentTimeMillis());
            Map<String, IotDevicePropertyPackPostReqDTO.EventValue> gatewayEvents = MapUtil
                    .<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                    .put("statusReport", gatewayEvent)
                    .build();

            // 2.3 构建【网关子设备】属性
            Map<String, Object> subDeviceProperties = MapUtil.<String, Object>builder()
                    .put("power", 100)
                    .build();

            // 2.4 构建【网关子设备】事件
            IotDevicePropertyPackPostReqDTO.EventValue subDeviceEvent = new IotDevicePropertyPackPostReqDTO.EventValue()
                    .setValue(MapUtil.builder().put("errorCode", 0).build())
                    .setTime(System.currentTimeMillis());
            Map<String, IotDevicePropertyPackPostReqDTO.EventValue> subDeviceEvents = MapUtil
                    .<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                    .put("healthCheck", subDeviceEvent)
                    .build();

            // 2.5 构建子设备数据
            IotDevicePropertyPackPostReqDTO.SubDeviceData subDeviceData = new IotDevicePropertyPackPostReqDTO.SubDeviceData()
                    .setIdentity(new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME))
                    .setProperties(subDeviceProperties)
                    .setEvents(subDeviceEvents);

            // 2.6 构建请求消息
            IotDevicePropertyPackPostReqDTO params = new IotDevicePropertyPackPostReqDTO()
                    .setProperties(gatewayProperties)
                    .setEvents(gatewayEvents)
                    .setSubDevices(ListUtil.of(subDeviceData));
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod(),
                    params);

            // 2.7 订阅 _reply 主题
            String replyTopic = String.format("/sys/%s/%s/thing/event/property/pack/post_reply",
                    GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
            subscribe(client, replyTopic);

            // 3. 发布消息并等待响应
            String topic = String.format("/sys/%s/%s/thing/event/property/pack/post",
                    GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
            IotDeviceMessage response = publishAndWaitReply(client, topic, request);
            log.info("[testPropertyPackPost][响应消息: {}]", response);
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
     * 连接并认证网关设备
     *
     * @return 已认证的 MQTT 客户端
     */
    private MqttClient connectAndAuth() throws Exception {
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
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
