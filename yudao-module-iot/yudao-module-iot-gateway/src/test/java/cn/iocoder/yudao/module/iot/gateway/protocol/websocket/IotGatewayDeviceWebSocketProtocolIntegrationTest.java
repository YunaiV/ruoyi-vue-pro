package cn.iocoder.yudao.module.iot.gateway.protocol.websocket;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
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
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketClient;
import io.vertx.core.http.WebSocketConnectOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * IoT 网关设备 WebSocket 协议集成测试（手动测试）
 *
 * <p>测试场景：网关设备（IotProductDeviceTypeEnum 的 GATEWAY 类型）通过 WebSocket 协议管理子设备拓扑关系
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（WebSocket 端口 8094）</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testAuth()} - 网关设备认证</li>
 *             <li>{@link #testTopoAdd()} - 添加子设备拓扑关系</li>
 *             <li>{@link #testTopoDelete()} - 删除子设备拓扑关系</li>
 *             <li>{@link #testTopoGet()} - 获取子设备拓扑关系</li>
 *             <li>{@link #testSubDeviceRegister()} - 子设备动态注册</li>
 *             <li>{@link #testPropertyPackPost()} - 批量上报属性（网关 + 子设备）</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * <p>注意：WebSocket 协议是有状态的长连接，认证成功后同一连接上的后续请求无需再携带认证信息
 *
 * @author 芋道源码
 */
@Slf4j
@Disabled
public class IotGatewayDeviceWebSocketProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8094;
    private static final String WS_PATH = "/ws";
    private static final int TIMEOUT_SECONDS = 5;

    private static Vertx vertx;

    // ===================== 编解码器选择 =====================

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

    // ===================== 认证测试 =====================

    /**
     * 网关设备认证测试
     */
    @Test
    public void testAuth() throws Exception {
        // 1.1 构建认证消息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(), "auth", authReqDTO, null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        String jsonMessage = StrUtil.utf8Str(payload);
        log.info("[testAuth][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 2.1 创建 WebSocket 连接（同步）
        WebSocket ws = createWebSocketConnection();
        log.info("[testAuth][WebSocket 连接成功]");

        // 2.2 发送并等待响应
        String response = sendAndReceive(ws, jsonMessage);

        // 3. 解码响应
        if (response != null) {
            IotDeviceMessage responseMessage = CODEC.decode(StrUtil.utf8Bytes(response));
            log.info("[testAuth][响应消息: {}]", responseMessage);
        } else {
            log.warn("[testAuth][未收到响应]");
        }

        // 4. 关闭连接
        ws.close();
    }

    // ===================== 拓扑管理测试 =====================

    /**
     * 添加子设备拓扑关系测试
     */
    @Test
    public void testTopoAdd() throws Exception {
        // 1.1 创建 WebSocket 连接（同步）
        WebSocket ws = createWebSocketConnection();
        log.info("[testTopoAdd][WebSocket 连接成功]");

        // 1.2 先进行认证
        IotDeviceMessage authResponse = authenticate(ws);
        log.info("[testTopoAdd][认证响应: {}]", authResponse);

        // 2.1 构建子设备认证信息
        IotDeviceAuthReqDTO subAuthInfo = IotDeviceAuthUtils.getAuthInfo(
                SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME, SUB_DEVICE_SECRET);
        IotDeviceAuthReqDTO subDeviceAuth = new IotDeviceAuthReqDTO()
                .setClientId(subAuthInfo.getClientId())
                .setUsername(subAuthInfo.getUsername())
                .setPassword(subAuthInfo.getPassword());
        // 2.2 构建请求参数
        IotDeviceTopoAddReqDTO params = new IotDeviceTopoAddReqDTO();
        params.setSubDevices(Collections.singletonList(subDeviceAuth));
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.TOPO_ADD.getMethod(),
                params,
                null, null, null);
        // 2.3 编码
        byte[] payload = CODEC.encode(request);
        String jsonMessage = StrUtil.utf8Str(payload);
        log.info("[testTopoAdd][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 3.1 发送并等待响应
        String response = sendAndReceive(ws, jsonMessage);
        // 3.2 解码响应
        if (response != null) {
            IotDeviceMessage responseMessage = CODEC.decode(StrUtil.utf8Bytes(response));
            log.info("[testTopoAdd][响应消息: {}]", responseMessage);
        } else {
            log.warn("[testTopoAdd][未收到响应]");
        }

        // 4. 关闭连接
        ws.close();
    }

    /**
     * 删除子设备拓扑关系测试
     */
    @Test
    public void testTopoDelete() throws Exception {
        // 1.1 创建 WebSocket 连接（同步）
        WebSocket ws = createWebSocketConnection();
        log.info("[testTopoDelete][WebSocket 连接成功]");

        // 1.2 先进行认证
        IotDeviceMessage authResponse = authenticate(ws);
        log.info("[testTopoDelete][认证响应: {}]", authResponse);

        // 2.1 构建请求参数
        IotDeviceTopoDeleteReqDTO params = new IotDeviceTopoDeleteReqDTO();
        params.setSubDevices(Collections.singletonList(
                new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME)));
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.TOPO_DELETE.getMethod(),
                params,
                null, null, null);
        // 2.2 编码
        byte[] payload = CODEC.encode(request);
        String jsonMessage = StrUtil.utf8Str(payload);
        log.info("[testTopoDelete][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 3.1 发送并等待响应
        String response = sendAndReceive(ws, jsonMessage);
        // 3.2 解码响应
        if (response != null) {
            IotDeviceMessage responseMessage = CODEC.decode(StrUtil.utf8Bytes(response));
            log.info("[testTopoDelete][响应消息: {}]", responseMessage);
        } else {
            log.warn("[testTopoDelete][未收到响应]");
        }

        // 4. 关闭连接
        ws.close();
    }

    /**
     * 获取子设备拓扑关系测试
     */
    @Test
    public void testTopoGet() throws Exception {
        // 1.1 创建 WebSocket 连接（同步）
        WebSocket ws = createWebSocketConnection();
        log.info("[testTopoGet][WebSocket 连接成功]");

        // 1.2 先进行认证
        IotDeviceMessage authResponse = authenticate(ws);
        log.info("[testTopoGet][认证响应: {}]", authResponse);

        // 2.1 构建请求参数
        IotDeviceTopoGetReqDTO params = new IotDeviceTopoGetReqDTO();
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.TOPO_GET.getMethod(),
                params,
                null, null, null);
        // 2.2 编码
        byte[] payload = CODEC.encode(request);
        String jsonMessage = StrUtil.utf8Str(payload);
        log.info("[testTopoGet][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 3.1 发送并等待响应
        String response = sendAndReceive(ws, jsonMessage);
        // 3.2 解码响应
        if (response != null) {
            IotDeviceMessage responseMessage = CODEC.decode(StrUtil.utf8Bytes(response));
            log.info("[testTopoGet][响应消息: {}]", responseMessage);
        } else {
            log.warn("[testTopoGet][未收到响应]");
        }

        // 4. 关闭连接
        ws.close();
    }

    // ===================== 子设备注册测试 =====================

    /**
     * 子设备动态注册测试
     */
    @Test
    public void testSubDeviceRegister() throws Exception {
        // 1.1 创建 WebSocket 连接（同步）
        WebSocket ws = createWebSocketConnection();
        log.info("[testSubDeviceRegister][WebSocket 连接成功]");

        // 1.2 先进行认证
        IotDeviceMessage authResponse = authenticate(ws);
        log.info("[testSubDeviceRegister][认证响应: {}]", authResponse);

        // 2.1 构建请求参数
        IotSubDeviceRegisterReqDTO subDevice = new IotSubDeviceRegisterReqDTO();
        subDevice.setProductKey(SUB_DEVICE_PRODUCT_KEY);
        subDevice.setDeviceName("mougezishebei-ws");
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod(),
                Collections.singletonList(subDevice),
                null, null, null);
        // 2.2 编码
        byte[] payload = CODEC.encode(request);
        String jsonMessage = StrUtil.utf8Str(payload);
        log.info("[testSubDeviceRegister][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 3.1 发送并等待响应
        String response = sendAndReceive(ws, jsonMessage);
        // 3.2 解码响应
        if (response != null) {
            IotDeviceMessage responseMessage = CODEC.decode(StrUtil.utf8Bytes(response));
            log.info("[testSubDeviceRegister][响应消息: {}]", responseMessage);
        } else {
            log.warn("[testSubDeviceRegister][未收到响应]");
        }

        // 4. 关闭连接
        ws.close();
    }

    // ===================== 批量上报测试 =====================

    /**
     * 批量上报属性测试（网关 + 子设备）
     */
    @Test
    public void testPropertyPackPost() throws Exception {
        // 1.1 创建 WebSocket 连接（同步）
        WebSocket ws = createWebSocketConnection();
        log.info("[testPropertyPackPost][WebSocket 连接成功]");

        // 1.2 先进行认证
        IotDeviceMessage authResponse = authenticate(ws);
        log.info("[testPropertyPackPost][认证响应: {}]", authResponse);

        // 2.1 构建【网关设备】自身属性
        Map<String, Object> gatewayProperties = MapUtil.<String, Object>builder()
                .put("temperature", 25.5)
                .build();
        // 2.2 构建【网关设备】自身事件
        IotDevicePropertyPackPostReqDTO.EventValue gatewayEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
        gatewayEvent.setValue(MapUtil.builder().put("message", "gateway started").build());
        gatewayEvent.setTime(System.currentTimeMillis());
        Map<String, IotDevicePropertyPackPostReqDTO.EventValue> gatewayEvents = MapUtil.<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                .put("statusReport", gatewayEvent)
                .build();
        // 2.3 构建【网关子设备】属性
        Map<String, Object> subDeviceProperties = MapUtil.<String, Object>builder()
                .put("power", 100)
                .build();
        // 2.4 构建【网关子设备】事件
        IotDevicePropertyPackPostReqDTO.EventValue subDeviceEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
        subDeviceEvent.setValue(MapUtil.builder().put("errorCode", 0).build());
        subDeviceEvent.setTime(System.currentTimeMillis());
        Map<String, IotDevicePropertyPackPostReqDTO.EventValue> subDeviceEvents = MapUtil.<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                .put("healthCheck", subDeviceEvent)
                .build();
        // 2.5 构建子设备数据
        IotDevicePropertyPackPostReqDTO.SubDeviceData subDeviceData = new IotDevicePropertyPackPostReqDTO.SubDeviceData();
        subDeviceData.setIdentity(new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME));
        subDeviceData.setProperties(subDeviceProperties);
        subDeviceData.setEvents(subDeviceEvents);
        // 2.6 构建请求参数
        IotDevicePropertyPackPostReqDTO params = new IotDevicePropertyPackPostReqDTO();
        params.setProperties(gatewayProperties);
        params.setEvents(gatewayEvents);
        params.setSubDevices(ListUtil.of(subDeviceData));
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod(),
                params,
                null, null, null);
        // 2.7 编码
        byte[] payload = CODEC.encode(request);
        String jsonMessage = StrUtil.utf8Str(payload);
        log.info("[testPropertyPackPost][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 3.1 发送并等待响应
        String response = sendAndReceive(ws, jsonMessage);
        // 3.2 解码响应
        if (response != null) {
            IotDeviceMessage responseMessage = CODEC.decode(StrUtil.utf8Bytes(response));
            log.info("[testPropertyPackPost][响应消息: {}]", responseMessage);
        } else {
            log.warn("[testPropertyPackPost][未收到响应]");
        }

        // 4. 关闭连接
        ws.close();
    }

    // ===================== 辅助方法 =====================

    /**
     * 创建 WebSocket 连接（同步）
     *
     * @return WebSocket 连接
     */
    private WebSocket createWebSocketConnection() throws Exception {
        WebSocketClient wsClient = vertx.createWebSocketClient();
        WebSocketConnectOptions options = new WebSocketConnectOptions()
                .setHost(SERVER_HOST)
                .setPort(SERVER_PORT)
                .setURI(WS_PATH);
        return wsClient.connect(options).toCompletionStage().toCompletableFuture().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 发送消息并等待响应（同步）
     *
     * @param ws      WebSocket 连接
     * @param message 请求消息
     * @return 响应消息
     */
    private String sendAndReceive(WebSocket ws, String message) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> responseRef = new AtomicReference<>();

        // 设置消息处理器
        ws.textMessageHandler(response -> {
            log.info("[sendAndReceive][收到响应: {}]", response);
            responseRef.set(response);
            latch.countDown();
        });

        // 发送请求
        log.info("[sendAndReceive][发送请求: {}]", message);
        ws.writeTextMessage(message);

        // 等待响应
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            log.warn("[sendAndReceive][等待响应超时]");
        }
        return responseRef.get();
    }

    /**
     * 执行网关设备认证（同步）
     *
     * @param ws WebSocket 连接
     * @return 认证响应消息
     */
    private IotDeviceMessage authenticate(WebSocket ws) throws Exception {
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(), "auth", authReqDTO, null, null, null);

        byte[] payload = CODEC.encode(request);
        String jsonMessage = StrUtil.utf8Str(payload);
        log.info("[authenticate][发送认证请求: {}]", jsonMessage);

        String response = sendAndReceive(ws, jsonMessage);
        if (response != null) {
            return CODEC.decode(StrUtil.utf8Bytes(response));
        }
        return null;
    }

}
