package cn.iocoder.yudao.module.iot.gateway.protocol.websocket;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
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
 * IoT 网关子设备 WebSocket 协议集成测试（手动测试）
 *
 * <p>测试场景：子设备（IotProductDeviceTypeEnum 的 SUB 类型）通过网关设备代理上报数据
 *
 * <p><b>重要说明：子设备无法直接连接平台，所有请求均由网关设备（Gateway）代为转发。</b>
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（WebSocket 端口 8094）</li>
 *     <li>确保子设备已通过 {@link IotGatewayDeviceWebSocketProtocolIntegrationTest#testTopoAdd()} 绑定到网关</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testAuth()} - 子设备认证</li>
 *             <li>{@link #testPropertyPost()} - 子设备属性上报（由网关代理转发）</li>
 *             <li>{@link #testEventPost()} - 子设备事件上报（由网关代理转发）</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * <p>注意：WebSocket 协议是有状态的长连接，认证成功后同一连接上的后续请求无需再携带认证信息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotGatewaySubDeviceWebSocketProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8094;
    private static final String WS_PATH = "/ws";
    private static final int TIMEOUT_SECONDS = 5;

    // 编解码器
    private static final IotDeviceMessageCodec CODEC = new IotWebSocketJsonDeviceMessageCodec();

    // ===================== 网关子设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================
    private static final String PRODUCT_KEY = "jAufEMTF1W6wnPhn";
    private static final String DEVICE_NAME = "chazuo-it";
    private static final String DEVICE_SECRET = "d46ef9b28ab14238b9c00a3a668032af";

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
     * 子设备认证测试
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
                IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
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

    // ===================== 子设备属性上报测试 =====================

    /**
     * 子设备属性上报测试
     */
    @Test
    public void testPropertyPost() throws Exception {
        executeAuthenticatedRequest("testPropertyPost", ws -> {
            log.info("[testPropertyPost][子设备属性上报 - 请求实际由 Gateway 代为转发]");
            return IotDeviceMessage.of(
                    IdUtil.fastSimpleUUID(),
                    IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                    IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                            .put("power", 100)
                            .put("status", "online")
                            .put("temperature", 36.5)
                            .build()),
                    null, null, null);
        });
    }

    // ===================== 子设备事件上报测试 =====================

    /**
     * 子设备事件上报测试
     */
    @Test
    public void testEventPost() throws Exception {
        executeAuthenticatedRequest("testEventPost", ws -> {
            log.info("[testEventPost][子设备事件上报 - 请求实际由 Gateway 代为转发]");
            return IotDeviceMessage.of(
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
                IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
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
