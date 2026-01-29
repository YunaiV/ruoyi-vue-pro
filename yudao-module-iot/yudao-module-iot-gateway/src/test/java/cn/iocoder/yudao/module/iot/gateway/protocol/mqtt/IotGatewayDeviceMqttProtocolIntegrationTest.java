package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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

    // ===================== 编解码器（MQTT 使用 Alink 协议） =====================

    private static final IotDeviceMessageCodec CODEC = new IotAlinkDeviceMessageCodec();

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
        CountDownLatch latch = new CountDownLatch(1);

        // 1. 构建认证信息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
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

        // 2.1 订阅 _reply 主题
        String replyTopic = String.format("/sys/%s/%s/thing/topo/add_reply",
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        subscribeReply(client, replyTopic);

        // 2.2 构建子设备认证信息
        IotDeviceAuthReqDTO subAuthInfo = IotDeviceAuthUtils.getAuthInfo(
                SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME, SUB_DEVICE_SECRET);
        IotDeviceAuthReqDTO subDeviceAuth = new IotDeviceAuthReqDTO()
                .setClientId(subAuthInfo.getClientId())
                .setUsername(subAuthInfo.getUsername())
                .setPassword(subAuthInfo.getPassword());

        // 2.3 构建请求消息
        IotDeviceTopoAddReqDTO params = new IotDeviceTopoAddReqDTO();
        params.setSubDevices(Collections.singletonList(subDeviceAuth));
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.TOPO_ADD.getMethod(),
                params,
                null, null, null);

        // 3. 发布消息并等待响应
        String topic = String.format("/sys/%s/%s/thing/topo/add",
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        IotDeviceMessage response = publishAndWaitReply(client, topic, request);
        log.info("[testTopoAdd][响应消息: {}]", response);

        // 4. 断开连接
        disconnect(client);
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

        // 2.1 订阅 _reply 主题
        String replyTopic = String.format("/sys/%s/%s/thing/topo/delete_reply",
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        subscribeReply(client, replyTopic);

        // 2.2 构建请求消息
        IotDeviceTopoDeleteReqDTO params = new IotDeviceTopoDeleteReqDTO();
        params.setSubDevices(Collections.singletonList(
                new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME)));
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.TOPO_DELETE.getMethod(),
                params,
                null, null, null);

        // 3. 发布消息并等待响应
        String topic = String.format("/sys/%s/%s/thing/topo/delete",
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        IotDeviceMessage response = publishAndWaitReply(client, topic, request);
        log.info("[testTopoDelete][响应消息: {}]", response);

        // 4. 断开连接
        disconnect(client);
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

        // 2.1 订阅 _reply 主题
        String replyTopic = String.format("/sys/%s/%s/thing/topo/get_reply",
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        subscribeReply(client, replyTopic);

        // 2.2 构建请求消息
        IotDeviceTopoGetReqDTO params = new IotDeviceTopoGetReqDTO();
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.TOPO_GET.getMethod(),
                params,
                null, null, null);

        // 3. 发布消息并等待响应
        String topic = String.format("/sys/%s/%s/thing/topo/get",
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        IotDeviceMessage response = publishAndWaitReply(client, topic, request);
        log.info("[testTopoGet][响应消息: {}]", response);

        // 4. 断开连接
        disconnect(client);
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

        // 2.1 订阅 _reply 主题
        String replyTopic = String.format("/sys/%s/%s/thing/auth/sub-device/register_reply",
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        subscribeReply(client, replyTopic);

        // 2.2 构建请求消息
        IotSubDeviceRegisterReqDTO subDevice = new IotSubDeviceRegisterReqDTO();
        subDevice.setProductKey(SUB_DEVICE_PRODUCT_KEY);
        subDevice.setDeviceName("mougezishebei-mqtt");
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod(),
                Collections.singletonList(subDevice),
                null, null, null);

        // 3. 发布消息并等待响应
        String topic = String.format("/sys/%s/%s/thing/auth/sub-device/register",
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        IotDeviceMessage response = publishAndWaitReply(client, topic, request);
        log.info("[testSubDeviceRegister][响应消息: {}]", response);

        // 4. 断开连接
        disconnect(client);
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

        // 2.1 订阅 _reply 主题
        String replyTopic = String.format("/sys/%s/%s/thing/event/property/pack/post_reply",
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        subscribeReply(client, replyTopic);

        // 2.2 构建【网关设备】自身属性
        Map<String, Object> gatewayProperties = MapUtil.<String, Object>builder()
                .put("temperature", 25.5)
                .build();

        // 2.3 构建【网关设备】自身事件
        IotDevicePropertyPackPostReqDTO.EventValue gatewayEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
        gatewayEvent.setValue(MapUtil.builder().put("message", "gateway started").build());
        gatewayEvent.setTime(System.currentTimeMillis());
        Map<String, IotDevicePropertyPackPostReqDTO.EventValue> gatewayEvents = MapUtil
                .<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                .put("statusReport", gatewayEvent)
                .build();

        // 2.4 构建【网关子设备】属性
        Map<String, Object> subDeviceProperties = MapUtil.<String, Object>builder()
                .put("power", 100)
                .build();

        // 2.5 构建【网关子设备】事件
        IotDevicePropertyPackPostReqDTO.EventValue subDeviceEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
        subDeviceEvent.setValue(MapUtil.builder().put("errorCode", 0).build());
        subDeviceEvent.setTime(System.currentTimeMillis());
        Map<String, IotDevicePropertyPackPostReqDTO.EventValue> subDeviceEvents = MapUtil
                .<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                .put("healthCheck", subDeviceEvent)
                .build();

        // 2.6 构建子设备数据
        IotDevicePropertyPackPostReqDTO.SubDeviceData subDeviceData = new IotDevicePropertyPackPostReqDTO.SubDeviceData();
        subDeviceData.setIdentity(new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME));
        subDeviceData.setProperties(subDeviceProperties);
        subDeviceData.setEvents(subDeviceEvents);

        // 2.7 构建请求消息
        IotDevicePropertyPackPostReqDTO params = new IotDevicePropertyPackPostReqDTO();
        params.setProperties(gatewayProperties);
        params.setEvents(gatewayEvents);
        params.setSubDevices(List.of(subDeviceData));
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod(),
                params,
                null, null, null);

        // 3. 发布消息并等待响应
        String topic = String.format("/sys/%s/%s/thing/event/property/pack/post",
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME);
        IotDeviceMessage response = publishAndWaitReply(client, topic, request);
        log.info("[testPropertyPackPost][响应消息: {}]", response);

        // 4. 断开连接
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
     * 连接并认证网关设备
     *
     * @return 已认证的 MQTT 客户端
     */
    private MqttClient connectAndAuth() throws Exception {
        // 1. 创建客户端并连接
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
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
