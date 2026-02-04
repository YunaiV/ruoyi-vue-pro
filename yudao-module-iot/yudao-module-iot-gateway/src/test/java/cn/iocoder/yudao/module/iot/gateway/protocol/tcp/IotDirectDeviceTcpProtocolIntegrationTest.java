package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import cn.iocoder.yudao.module.iot.core.util.IotProductAuthUtils;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpCodecTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodecFactory;
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.json.IotJsonSerializer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * IoT 直连设备 TCP 协议集成测试（手动测试）
 *
 * <p>测试场景：直连设备（IotProductDeviceTypeEnum 的 DIRECT 类型）通过 TCP 协议直接连接平台
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（TCP 端口 8091）</li>
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
 * <p>注意：TCP 协议是有状态的长连接，认证成功后同一连接上的后续请求无需再携带认证信息
 *
 * @author 芋道源码
 */
@Slf4j
@Disabled
public class IotDirectDeviceTcpProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8091;
    private static final int TIMEOUT_MS = 5000;

    private static Vertx vertx;
    private static NetClient netClient;

    // ===================== 编解码器 =====================

    /**
     * 消息序列化器
     */
    private static final IotMessageSerializer SERIALIZER = new IotJsonSerializer();

    /**
     * TCP 帧编解码器
     */
    private static final IotTcpFrameCodec FRAME_CODEC = IotTcpFrameCodecFactory.create(
            new IotTcpConfig.CodecConfig()
                    .setType(IotTcpCodecTypeEnum.DELIMITER.getType())
                    .setDelimiter("\\n")
//                    .setType(IotTcpCodecTypeEnum.LENGTH_FIELD.getType())
//                    .setLengthFieldOffset(0)
//                    .setLengthFieldLength(4)
//                    .setLengthAdjustment(0)
//                    .setInitialBytesToStrip(4)
//                    .setType(IotTcpCodecTypeEnum.LENGTH_FIELD.getType())
//                    .setFixedLength(256)
    );

    // ===================== 直连设备信息（根据实际情况修改，从 iot_device 表查询） =====================

    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String DEVICE_SECRET = "0baa4c2ecc104ae1a26b4070c218bdf3";

    @BeforeAll
    static void setUp() {
        vertx = Vertx.vertx();
        NetClientOptions options = new NetClientOptions()
                .setConnectTimeout(TIMEOUT_MS)
                .setIdleTimeout(TIMEOUT_MS);
        netClient = vertx.createNetClient(options);
    }

    @AfterAll
    static void tearDown() {
        if (netClient != null) {
            netClient.close();
        }
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
        // 1. 构建认证消息
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        IotDeviceMessage request = IotDeviceMessage.requestOf("auth", authReqDTO);

        // 2. 发送并接收响应
        NetSocket socket = connect().get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        try {
            IotDeviceMessage response = sendAndReceive(socket, request);
            log.info("[testAuth][响应消息: {}]", response);
        } finally {
            socket.close();
        }
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
        // 1. 构建注册消息
        String deviceName = "test-tcp-" + System.currentTimeMillis();
        String productSecret = "test-product-secret"; // 替换为实际的 productSecret
        String sign = IotProductAuthUtils.buildSign(PRODUCT_KEY, deviceName, productSecret);
        IotDeviceRegisterReqDTO registerReqDTO = new IotDeviceRegisterReqDTO()
                .setProductKey(PRODUCT_KEY)
                .setDeviceName(deviceName)
                .setSign(sign);
        IotDeviceMessage request = IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod(), registerReqDTO);

        // 2. 发送并接收响应
        NetSocket socket = connect().get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        try {
            IotDeviceMessage response = sendAndReceive(socket, request);
            log.info("[testDeviceRegister][响应消息: {}]", response);
            log.info("[testDeviceRegister][成功后可使用返回的 deviceSecret 进行一机一密认证]");
        } finally {
            socket.close();
        }
    }

    // ===================== 直连设备属性上报测试 =====================

    /**
     * 属性上报测试
     */
    @Test
    public void testPropertyPost() throws Exception {
        NetSocket socket = connect().get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        try {
            // 1. 先进行认证
            IotDeviceMessage authResponse = authenticate(socket);
            log.info("[testPropertyPost][认证响应: {}]", authResponse);

            // 2. 构建属性上报消息
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                    IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                            .put("width", 1)
                            .put("height", "2")
                            .build()));

            // 3. 发送并接收响应
            IotDeviceMessage response = sendAndReceive(socket, request);
            log.info("[testPropertyPost][响应消息: {}]", response);
        } finally {
            socket.close();
        }
    }

    // ===================== 直连设备事件上报测试 =====================

    /**
     * 事件上报测试
     */
    @Test
    public void testEventPost() throws Exception {
        NetSocket socket = connect().get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        try {
            // 1. 先进行认证
            IotDeviceMessage authResponse = authenticate(socket);
            log.info("[testEventPost][认证响应: {}]", authResponse);

            // 2. 构建事件上报消息
            IotDeviceMessage request = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                    IotDeviceEventPostReqDTO.of(
                            "eat",
                            MapUtil.<String, Object>builder().put("rice", 3).build(),
                            System.currentTimeMillis()));

            // 3. 发送并接收响应
            IotDeviceMessage response = sendAndReceive(socket, request);
            log.info("[testEventPost][响应消息: {}]", response);
        } finally {
            socket.close();
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 建立 TCP 连接
     *
     * @return 连接 Future
     */
    private CompletableFuture<NetSocket> connect() {
        CompletableFuture<NetSocket> future = new CompletableFuture<>();
        netClient.connect(SERVER_PORT, SERVER_HOST)
                .onSuccess(future::complete)
                .onFailure(future::completeExceptionally);
        return future;
    }

    /**
     * 执行设备认证
     *
     * @param socket TCP 连接
     * @return 认证响应消息
     */
    private IotDeviceMessage authenticate(NetSocket socket) throws Exception {
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        IotDeviceMessage request = IotDeviceMessage.requestOf("auth", authInfo);
        return sendAndReceive(socket, request);
    }

    /**
     * 发送消息并接收响应
     *
     * @param socket  TCP 连接
     * @param request 请求消息
     * @return 响应消息
     */
    private IotDeviceMessage sendAndReceive(NetSocket socket, IotDeviceMessage request) throws Exception {
        // 1. 使用 FRAME_CODEC 创建解码器
        CompletableFuture<IotDeviceMessage> responseFuture = new CompletableFuture<>();
        RecordParser parser = FRAME_CODEC.createDecodeParser(buffer -> {
            try {
                // 反序列化响应
                IotDeviceMessage response = SERIALIZER.deserialize(buffer.getBytes());
                responseFuture.complete(response);
            } catch (Exception e) {
                responseFuture.completeExceptionally(e);
            }
        });
        socket.handler(parser);

        // 2.1 序列化 + 帧编码
        byte[] serializedData = SERIALIZER.serialize(request);
        Buffer frameData = FRAME_CODEC.encode(serializedData);
        log.info("[sendAndReceive][发送消息: {}，数据长度: {} 字节]", request.getMethod(), frameData.length());
        // 2.2 发送请求
        socket.write(frameData);

        // 3. 等待响应
        IotDeviceMessage response = responseFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        log.info("[sendAndReceive][收到响应，数据长度: {} 字节]", SERIALIZER.serialize(response).length);
        return response;
    }

}
