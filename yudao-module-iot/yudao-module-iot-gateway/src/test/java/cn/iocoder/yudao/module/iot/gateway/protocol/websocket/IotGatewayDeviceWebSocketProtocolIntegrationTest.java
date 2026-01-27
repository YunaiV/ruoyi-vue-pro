package cn.iocoder.yudao.module.iot.gateway.protocol.websocket;

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
import cn.iocoder.yudao.module.iot.gateway.codec.websocket.IotWebSocketJsonDeviceMessageCodec;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
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
public class IotGatewayDeviceWebSocketProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8094;
    private static final String WS_PATH = "/ws";
    private static final int TIMEOUT_SECONDS = 5;

    private static Vertx vertx;

    // ===================== 编解码器选择 =====================

    private static final IotDeviceMessageCodec CODEC = new IotWebSocketJsonDeviceMessageCodec();

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
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> responseRef = new AtomicReference<>();

        HttpClient client = vertx.createHttpClient();
        WebSocketConnectOptions options = new WebSocketConnectOptions()
                .setHost(SERVER_HOST)
                .setPort(SERVER_PORT)
                .setURI(WS_PATH);

        client.webSocket(options).onComplete(ar -> {
            if (ar.succeeded()) {
                WebSocket ws = ar.result();
                log.info("[testAuth][WebSocket 连接成功]");

                ws.textMessageHandler(message -> {
                    log.info("[testAuth][收到响应: {}]", message);
                    responseRef.set(message);
                    ws.close();
                    latch.countDown();
                });

                // 构建认证消息
                IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                        GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
                IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                        .setClientId(authInfo.getClientId())
                        .setUsername(authInfo.getUsername())
                        .setPassword(authInfo.getPassword());
                IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(), "auth", authReqDTO, null, null, null);

                byte[] payload = CODEC.encode(request);
                String jsonMessage = new String(payload, StandardCharsets.UTF_8);
                log.info("[testAuth][发送认证请求: {}]", jsonMessage);
                ws.writeTextMessage(jsonMessage);
            } else {
                log.error("[testAuth][WebSocket 连接失败]", ar.cause());
                latch.countDown();
            }
        });

        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (completed && responseRef.get() != null) {
            IotDeviceMessage response = CODEC.decode(responseRef.get().getBytes(StandardCharsets.UTF_8));
            log.info("[testAuth][解码响应: {}]", response);
        } else {
            log.warn("[testAuth][测试超时或未收到响应]");
        }
    }

    // ===================== 拓扑管理测试 =====================

    /**
     * 添加子设备拓扑关系测试
     */
    @Test
    public void testTopoAdd() throws Exception {
        executeAuthenticatedRequest("testTopoAdd", ws -> {
            // 构建子设备认证信息
            IotDeviceAuthReqDTO subAuthInfo = IotDeviceAuthUtils.getAuthInfo(
                    SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME, SUB_DEVICE_SECRET);
            IotDeviceAuthReqDTO subDeviceAuth = new IotDeviceAuthReqDTO()
                    .setClientId(subAuthInfo.getClientId())
                    .setUsername(subAuthInfo.getUsername())
                    .setPassword(subAuthInfo.getPassword());
            // 构建请求参数
            IotDeviceTopoAddReqDTO params = new IotDeviceTopoAddReqDTO();
            params.setSubDevices(Collections.singletonList(subDeviceAuth));
            return IotDeviceMessage.of(
                    IdUtil.fastSimpleUUID(),
                    IotDeviceMessageMethodEnum.TOPO_ADD.getMethod(),
                    params,
                    null, null, null);
        });
    }

    /**
     * 删除子设备拓扑关系测试
     */
    @Test
    public void testTopoDelete() throws Exception {
        executeAuthenticatedRequest("testTopoDelete", ws -> {
            IotDeviceTopoDeleteReqDTO params = new IotDeviceTopoDeleteReqDTO();
            params.setSubDevices(Collections.singletonList(
                    new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME)));
            return IotDeviceMessage.of(
                    IdUtil.fastSimpleUUID(),
                    IotDeviceMessageMethodEnum.TOPO_DELETE.getMethod(),
                    params,
                    null, null, null);
        });
    }

    /**
     * 获取子设备拓扑关系测试
     */
    @Test
    public void testTopoGet() throws Exception {
        executeAuthenticatedRequest("testTopoGet", ws -> {
            IotDeviceTopoGetReqDTO params = new IotDeviceTopoGetReqDTO();
            return IotDeviceMessage.of(
                    IdUtil.fastSimpleUUID(),
                    IotDeviceMessageMethodEnum.TOPO_GET.getMethod(),
                    params,
                    null, null, null);
        });
    }

    // ===================== 子设备注册测试 =====================

    /**
     * 子设备动态注册测试
     */
    @Test
    public void testSubDeviceRegister() throws Exception {
        executeAuthenticatedRequest("testSubDeviceRegister", ws -> {
            IotSubDeviceRegisterReqDTO subDevice = new IotSubDeviceRegisterReqDTO();
            subDevice.setProductKey(SUB_DEVICE_PRODUCT_KEY);
            subDevice.setDeviceName("mougezishebei-ws");
            return IotDeviceMessage.of(
                    IdUtil.fastSimpleUUID(),
                    IotDeviceMessageMethodEnum.SUB_DEVICE_REGISTER.getMethod(),
                    Collections.singletonList(subDevice),
                    null, null, null);
        });
    }

    // ===================== 批量上报测试 =====================

    /**
     * 批量上报属性测试（网关 + 子设备）
     */
    @Test
    public void testPropertyPackPost() throws Exception {
        executeAuthenticatedRequest("testPropertyPackPost", ws -> {
            // 构建【网关设备】自身属性
            Map<String, Object> gatewayProperties = MapUtil.<String, Object>builder()
                    .put("temperature", 25.5)
                    .build();
            // 构建【网关设备】自身事件
            IotDevicePropertyPackPostReqDTO.EventValue gatewayEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
            gatewayEvent.setValue(MapUtil.builder().put("message", "gateway started").build());
            gatewayEvent.setTime(System.currentTimeMillis());
            Map<String, IotDevicePropertyPackPostReqDTO.EventValue> gatewayEvents = MapUtil.<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                    .put("statusReport", gatewayEvent)
                    .build();
            // 构建【网关子设备】属性
            Map<String, Object> subDeviceProperties = MapUtil.<String, Object>builder()
                    .put("power", 100)
                    .build();
            // 构建【网关子设备】事件
            IotDevicePropertyPackPostReqDTO.EventValue subDeviceEvent = new IotDevicePropertyPackPostReqDTO.EventValue();
            subDeviceEvent.setValue(MapUtil.builder().put("errorCode", 0).build());
            subDeviceEvent.setTime(System.currentTimeMillis());
            Map<String, IotDevicePropertyPackPostReqDTO.EventValue> subDeviceEvents = MapUtil.<String, IotDevicePropertyPackPostReqDTO.EventValue>builder()
                    .put("healthCheck", subDeviceEvent)
                    .build();
            // 构建子设备数据
            IotDevicePropertyPackPostReqDTO.SubDeviceData subDeviceData = new IotDevicePropertyPackPostReqDTO.SubDeviceData();
            subDeviceData.setIdentity(new IotDeviceIdentity(SUB_DEVICE_PRODUCT_KEY, SUB_DEVICE_NAME));
            subDeviceData.setProperties(subDeviceProperties);
            subDeviceData.setEvents(subDeviceEvents);
            // 构建请求参数
            IotDevicePropertyPackPostReqDTO params = new IotDevicePropertyPackPostReqDTO();
            params.setProperties(gatewayProperties);
            params.setEvents(gatewayEvents);
            params.setSubDevices(List.of(subDeviceData));
            return IotDeviceMessage.of(
                    IdUtil.fastSimpleUUID(),
                    IotDeviceMessageMethodEnum.PROPERTY_PACK_POST.getMethod(),
                    params,
                    null, null, null);
        });
    }

    // ===================== 辅助方法 =====================

    /**
     * 执行需要认证的请求
     *
     * @param testName        测试名称
     * @param requestSupplier 请求消息提供者
     */
    private void executeAuthenticatedRequest(String testName, java.util.function.Function<WebSocket, IotDeviceMessage> requestSupplier) throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<String> authResponseRef = new AtomicReference<>();
        AtomicReference<String> businessResponseRef = new AtomicReference<>();

        HttpClient client = vertx.createHttpClient();
        WebSocketConnectOptions options = new WebSocketConnectOptions()
                .setHost(SERVER_HOST)
                .setPort(SERVER_PORT)
                .setURI(WS_PATH);

        client.webSocket(options).onComplete(ar -> {
            if (ar.succeeded()) {
                WebSocket ws = ar.result();
                log.info("[{}][WebSocket 连接成功]", testName);

                final boolean[] authenticated = {false};

                ws.textMessageHandler(message -> {
                    log.info("[{}][收到响应: {}]", testName, message);
                    if (!authenticated[0]) {
                        authResponseRef.set(message);
                        authenticated[0] = true;
                        latch.countDown();

                        // 认证成功后发送业务请求
                        IotDeviceMessage businessRequest = requestSupplier.apply(ws);
                        byte[] payload = CODEC.encode(businessRequest);
                        String jsonMessage = new String(payload, StandardCharsets.UTF_8);
                        log.info("[{}][发送业务请求: {}]", testName, jsonMessage);
                        ws.writeTextMessage(jsonMessage);
                    } else {
                        businessResponseRef.set(message);
                        ws.close();
                        latch.countDown();
                    }
                });

                // 先发送认证请求
                IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(
                        GATEWAY_PRODUCT_KEY, GATEWAY_DEVICE_NAME, GATEWAY_DEVICE_SECRET);
                IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                        .setClientId(authInfo.getClientId())
                        .setUsername(authInfo.getUsername())
                        .setPassword(authInfo.getPassword());
                IotDeviceMessage authRequest = IotDeviceMessage.of(IdUtil.fastSimpleUUID(), "auth", authReqDTO, null, null, null);

                byte[] payload = CODEC.encode(authRequest);
                String jsonMessage = new String(payload, StandardCharsets.UTF_8);
                log.info("[{}][发送认证请求: {}]", testName, jsonMessage);
                ws.writeTextMessage(jsonMessage);
            } else {
                log.error("[{}][WebSocket 连接失败]", testName, ar.cause());
                latch.countDown();
                latch.countDown();
            }
        });

        boolean completed = latch.await(TIMEOUT_SECONDS * 2, TimeUnit.SECONDS);
        if (completed) {
            if (authResponseRef.get() != null) {
                IotDeviceMessage authResponse = CODEC.decode(authResponseRef.get().getBytes(StandardCharsets.UTF_8));
                log.info("[{}][认证响应: {}]", testName, authResponse);
            }
            if (businessResponseRef.get() != null) {
                IotDeviceMessage businessResponse = CODEC.decode(businessResponseRef.get().getBytes(StandardCharsets.UTF_8));
                log.info("[{}][业务响应: {}]", testName, businessResponse);
            }
        } else {
            log.warn("[{}][测试超时]", testName);
        }
    }

}
