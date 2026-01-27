package cn.iocoder.yudao.module.iot.gateway.protocol.websocket;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * IoT 直连设备 WebSocket 协议集成测试（手动测试）
 *
 * <p>测试场景：直连设备（IotProductDeviceTypeEnum 的 DIRECT 类型）通过 WebSocket 协议直接连接平台
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（WebSocket 端口 8094）</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testAuth()} - 设备认证</li>
 *             <li>{@link #testDeviceRegister()} - 设备动态注册（一型一密）</li>
 *             <li>{@link #testPropertyPost()} - 设备属性上报</li>
 *             <li>{@link #testEventPost()} - 设备事件上报</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * <p>注意：WebSocket 协议是有状态的长连接，认证成功后同一连接上的后续请求无需再携带认证信息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotDirectDeviceWebSocketProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8094;
    private static final String WS_PATH = "/ws";
    private static final int TIMEOUT_SECONDS = 5;

    // 编解码器
    private static final IotDeviceMessageCodec CODEC = new IotWebSocketJsonDeviceMessageCodec();

    // ===================== 直连设备信息（根据实际情况修改，从 iot_device 表查询） =====================
    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String DEVICE_SECRET = "0baa4c2ecc104ae1a26b4070c218bdf3";

    // Vert.x 实例
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

    // ===================== 认证测试 =====================

    /**
     * 认证测试：获取设备 Token
     */
    @Test
    public void testAuth() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> responseRef = new AtomicReference<>();

        // 1. 创建 WebSocket 连接
        HttpClient client = vertx.createHttpClient();
        WebSocketConnectOptions options = new WebSocketConnectOptions()
                .setHost(SERVER_HOST)
                .setPort(SERVER_PORT)
                .setURI(WS_PATH);

        client.webSocket(options).onComplete(ar -> {
            if (ar.succeeded()) {
                WebSocket ws = ar.result();
                log.info("[testAuth][WebSocket 连接成功]");

                // 设置消息处理器
                ws.textMessageHandler(message -> {
                    log.info("[testAuth][收到响应: {}]", message);
                    responseRef.set(message);
                    ws.close();
                    latch.countDown();
                });

                // 2. 构建认证消息
                IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
                IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                        .setClientId(authInfo.getClientId())
                        .setUsername(authInfo.getUsername())
                        .setPassword(authInfo.getPassword());
                IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(), "auth", authReqDTO, null, null, null);

                // 3. 编码并发送
                byte[] payload = CODEC.encode(request);
                String jsonMessage = new String(payload, StandardCharsets.UTF_8);
                log.info("[testAuth][发送认证请求: {}]", jsonMessage);
                ws.writeTextMessage(jsonMessage);
            } else {
                log.error("[testAuth][WebSocket 连接失败]", ar.cause());
                latch.countDown();
            }
        });

        // 4. 等待响应
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (completed && responseRef.get() != null) {
            IotDeviceMessage response = CODEC.decode(responseRef.get().getBytes(StandardCharsets.UTF_8));
            log.info("[testAuth][解码响应: {}]", response);
        } else {
            log.warn("[testAuth][测试超时或未收到响应]");
        }
    }

    // ===================== 动态注册测试 =====================

    /**
     * 直连设备动态注册测试（一型一密）
     */
    @Test
    public void testDeviceRegister() throws Exception {
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
                log.info("[testDeviceRegister][WebSocket 连接成功]");

                ws.textMessageHandler(message -> {
                    log.info("[testDeviceRegister][收到响应: {}]", message);
                    responseRef.set(message);
                    ws.close();
                    latch.countDown();
                });

                // 构建注册消息
                IotDeviceRegisterReqDTO registerReqDTO = new IotDeviceRegisterReqDTO();
                registerReqDTO.setProductKey(PRODUCT_KEY);
                registerReqDTO.setDeviceName("test-ws-" + System.currentTimeMillis());
                registerReqDTO.setProductSecret("test-product-secret");
                IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(),
                        IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod(), registerReqDTO, null, null, null);

                byte[] payload = CODEC.encode(request);
                String jsonMessage = new String(payload, StandardCharsets.UTF_8);
                log.info("[testDeviceRegister][发送注册请求: {}]", jsonMessage);
                ws.writeTextMessage(jsonMessage);
            } else {
                log.error("[testDeviceRegister][WebSocket 连接失败]", ar.cause());
                latch.countDown();
            }
        });

        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (completed && responseRef.get() != null) {
            IotDeviceMessage response = CODEC.decode(responseRef.get().getBytes(StandardCharsets.UTF_8));
            log.info("[testDeviceRegister][解码响应: {}]", response);
        } else {
            log.warn("[testDeviceRegister][测试超时或未收到响应]");
        }
    }

    // ===================== 直连设备属性上报测试 =====================

    /**
     * 属性上报测试
     */
    @Test
    public void testPropertyPost() throws Exception {
        CountDownLatch latch = new CountDownLatch(2); // 认证 + 属性上报
        AtomicReference<String> authResponseRef = new AtomicReference<>();
        AtomicReference<String> propertyResponseRef = new AtomicReference<>();

        HttpClient client = vertx.createHttpClient();
        WebSocketConnectOptions options = new WebSocketConnectOptions()
                .setHost(SERVER_HOST)
                .setPort(SERVER_PORT)
                .setURI(WS_PATH);

        client.webSocket(options).onComplete(ar -> {
            if (ar.succeeded()) {
                WebSocket ws = ar.result();
                log.info("[testPropertyPost][WebSocket 连接成功]");

                final boolean[] authenticated = {false};

                ws.textMessageHandler(message -> {
                    log.info("[testPropertyPost][收到响应: {}]", message);
                    if (!authenticated[0]) {
                        authResponseRef.set(message);
                        authenticated[0] = true;
                        latch.countDown();

                        // 认证成功后发送属性上报
                        IotDeviceMessage propertyRequest = IotDeviceMessage.of(
                                IdUtil.fastSimpleUUID(),
                                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                                IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                                        .put("width", 1)
                                        .put("height", "2")
                                        .build()),
                                null, null, null);
                        byte[] payload = CODEC.encode(propertyRequest);
                        String jsonMessage = new String(payload, StandardCharsets.UTF_8);
                        log.info("[testPropertyPost][发送属性上报请求: {}]", jsonMessage);
                        ws.writeTextMessage(jsonMessage);
                    } else {
                        propertyResponseRef.set(message);
                        ws.close();
                        latch.countDown();
                    }
                });

                // 先发送认证请求
                IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
                IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                        .setClientId(authInfo.getClientId())
                        .setUsername(authInfo.getUsername())
                        .setPassword(authInfo.getPassword());
                IotDeviceMessage authRequest = IotDeviceMessage.of(IdUtil.fastSimpleUUID(), "auth", authReqDTO, null, null, null);

                byte[] payload = CODEC.encode(authRequest);
                String jsonMessage = new String(payload, StandardCharsets.UTF_8);
                log.info("[testPropertyPost][发送认证请求: {}]", jsonMessage);
                ws.writeTextMessage(jsonMessage);
            } else {
                log.error("[testPropertyPost][WebSocket 连接失败]", ar.cause());
                latch.countDown();
                latch.countDown();
            }
        });

        boolean completed = latch.await(TIMEOUT_SECONDS * 2, TimeUnit.SECONDS);
        if (completed) {
            if (authResponseRef.get() != null) {
                IotDeviceMessage authResponse = CODEC.decode(authResponseRef.get().getBytes(StandardCharsets.UTF_8));
                log.info("[testPropertyPost][认证响应: {}]", authResponse);
            }
            if (propertyResponseRef.get() != null) {
                IotDeviceMessage propertyResponse = CODEC.decode(propertyResponseRef.get().getBytes(StandardCharsets.UTF_8));
                log.info("[testPropertyPost][属性上报响应: {}]", propertyResponse);
            }
        } else {
            log.warn("[testPropertyPost][测试超时]");
        }
    }

    // ===================== 直连设备事件上报测试 =====================

    /**
     * 事件上报测试
     */
    @Test
    public void testEventPost() throws Exception {
        CountDownLatch latch = new CountDownLatch(2); // 认证 + 事件上报
        AtomicReference<String> authResponseRef = new AtomicReference<>();
        AtomicReference<String> eventResponseRef = new AtomicReference<>();

        HttpClient client = vertx.createHttpClient();
        WebSocketConnectOptions options = new WebSocketConnectOptions()
                .setHost(SERVER_HOST)
                .setPort(SERVER_PORT)
                .setURI(WS_PATH);

        client.webSocket(options).onComplete(ar -> {
            if (ar.succeeded()) {
                WebSocket ws = ar.result();
                log.info("[testEventPost][WebSocket 连接成功]");

                final boolean[] authenticated = {false};

                ws.textMessageHandler(message -> {
                    log.info("[testEventPost][收到响应: {}]", message);
                    if (!authenticated[0]) {
                        authResponseRef.set(message);
                        authenticated[0] = true;
                        latch.countDown();

                        // 认证成功后发送事件上报
                        IotDeviceMessage eventRequest = IotDeviceMessage.of(
                                IdUtil.fastSimpleUUID(),
                                IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                                IotDeviceEventPostReqDTO.of(
                                        "eat",
                                        MapUtil.<String, Object>builder().put("rice", 3).build(),
                                        System.currentTimeMillis()),
                                null, null, null);
                        byte[] payload = CODEC.encode(eventRequest);
                        String jsonMessage = new String(payload, StandardCharsets.UTF_8);
                        log.info("[testEventPost][发送事件上报请求: {}]", jsonMessage);
                        ws.writeTextMessage(jsonMessage);
                    } else {
                        eventResponseRef.set(message);
                        ws.close();
                        latch.countDown();
                    }
                });

                // 先发送认证请求
                IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
                IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                        .setClientId(authInfo.getClientId())
                        .setUsername(authInfo.getUsername())
                        .setPassword(authInfo.getPassword());
                IotDeviceMessage authRequest = IotDeviceMessage.of(IdUtil.fastSimpleUUID(), "auth", authReqDTO, null, null, null);

                byte[] payload = CODEC.encode(authRequest);
                String jsonMessage = new String(payload, StandardCharsets.UTF_8);
                log.info("[testEventPost][发送认证请求: {}]", jsonMessage);
                ws.writeTextMessage(jsonMessage);
            } else {
                log.error("[testEventPost][WebSocket 连接失败]", ar.cause());
                latch.countDown();
                latch.countDown();
            }
        });

        boolean completed = latch.await(TIMEOUT_SECONDS * 2, TimeUnit.SECONDS);
        if (completed) {
            if (authResponseRef.get() != null) {
                IotDeviceMessage authResponse = CODEC.decode(authResponseRef.get().getBytes(StandardCharsets.UTF_8));
                log.info("[testEventPost][认证响应: {}]", authResponse);
            }
            if (eventResponseRef.get() != null) {
                IotDeviceMessage eventResponse = CODEC.decode(eventResponseRef.get().getBytes(StandardCharsets.UTF_8));
                log.info("[testEventPost][事件上报响应: {}]", eventResponse);
            }
        } else {
            log.warn("[testEventPost][测试超时]");
        }
    }

}
