package cn.iocoder.yudao.module.iot.gateway.protocol.websocket;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
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
@Disabled
public class IotDirectDeviceWebSocketProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8094;
    private static final String WS_PATH = "/ws";
    private static final int TIMEOUT_SECONDS = 5;

    private static Vertx vertx;

    // ===================== 编解码器选择 =====================

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

    // ===================== 认证测试 =====================

    /**
     * 认证测试：获取设备 Token
     */
    @Test
    public void testAuth() throws Exception {
        // 1.1 构建认证消息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
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

    // ===================== 动态注册测试 =====================

    /**
     * 直连设备动态注册测试（一型一密）
     * <p>
     * 使用产品密钥（productSecret）验证身份，成功后返回设备密钥（deviceSecret）
     * <p>
     * 注意：此接口不需要认证
     */
    @Test
    public void testDeviceRegister() throws Exception {
        // 1.1 构建注册消息
        IotDeviceRegisterReqDTO registerReqDTO = new IotDeviceRegisterReqDTO();
        registerReqDTO.setProductKey(PRODUCT_KEY);
        registerReqDTO.setDeviceName("test-ws-" + System.currentTimeMillis());
        registerReqDTO.setProductSecret("test-product-secret");
        IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod(), registerReqDTO, null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        String jsonMessage = StrUtil.utf8Str(payload);
        log.info("[testDeviceRegister][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 2.1 创建 WebSocket 连接（同步）
        WebSocket ws = createWebSocketConnection();
        log.info("[testDeviceRegister][WebSocket 连接成功]");

        // 2.2 发送并等待响应
        String response = sendAndReceive(ws, jsonMessage);

        // 3. 解码响应
        if (response != null) {
            IotDeviceMessage responseMessage = CODEC.decode(StrUtil.utf8Bytes(response));
            log.info("[testDeviceRegister][响应消息: {}]", responseMessage);
            log.info("[testDeviceRegister][成功后可使用返回的 deviceSecret 进行一机一密认证]");
        } else {
            log.warn("[testDeviceRegister][未收到响应]");
        }

        // 4. 关闭连接
        ws.close();
    }

    // ===================== 直连设备属性上报测试 =====================

    /**
     * 属性上报测试
     */
    @Test
    public void testPropertyPost() throws Exception {
        // 1.1 创建 WebSocket 连接（同步）
        WebSocket ws = createWebSocketConnection();
        log.info("[testPropertyPost][WebSocket 连接成功]");

        // 1.2 先进行认证
        IotDeviceMessage authResponse = authenticate(ws);
        log.info("[testPropertyPost][认证响应: {}]", authResponse);

        // 2.1 构建属性上报消息
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                        .put("width", 1)
                        .put("height", "2")
                        .build()),
                null, null, null);
        // 2.2 编码
        byte[] payload = CODEC.encode(request);
        String jsonMessage = StrUtil.utf8Str(payload);
        log.info("[testPropertyPost][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 3.1 发送并等待响应
        String response = sendAndReceive(ws, jsonMessage);
        // 3.2 解码响应
        if (response != null) {
            IotDeviceMessage responseMessage = CODEC.decode(StrUtil.utf8Bytes(response));
            log.info("[testPropertyPost][响应消息: {}]", responseMessage);
        } else {
            log.warn("[testPropertyPost][未收到响应]");
        }

        // 4. 关闭连接
        ws.close();
    }

    // ===================== 直连设备事件上报测试 =====================

    /**
     * 事件上报测试
     */
    @Test
    public void testEventPost() throws Exception {
        // 1.1 创建 WebSocket 连接（同步）
        WebSocket ws = createWebSocketConnection();
        log.info("[testEventPost][WebSocket 连接成功]");

        // 1.2 先进行认证
        IotDeviceMessage authResponse = authenticate(ws);
        log.info("[testEventPost][认证响应: {}]", authResponse);

        // 2.1 构建事件上报消息
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                IotDeviceEventPostReqDTO.of(
                        "eat",
                        MapUtil.<String, Object>builder().put("rice", 3).build(),
                        System.currentTimeMillis()),
                null, null, null);
        // 2.2 编码
        byte[] payload = CODEC.encode(request);
        String jsonMessage = StrUtil.utf8Str(payload);
        log.info("[testEventPost][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 3.1 发送并等待响应
        String response = sendAndReceive(ws, jsonMessage);
        // 3.2 解码响应
        if (response != null) {
            IotDeviceMessage responseMessage = CODEC.decode(StrUtil.utf8Bytes(response));
            log.info("[testEventPost][响应消息: {}]", responseMessage);
        } else {
            log.warn("[testEventPost][未收到响应]");
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
    public static String sendAndReceive(WebSocket ws, String message) throws Exception {
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
     * 执行设备认证（同步）
     *
     * @param ws WebSocket 连接
     * @return 认证响应消息
     */
    private IotDeviceMessage authenticate(WebSocket ws) throws Exception {
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
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
