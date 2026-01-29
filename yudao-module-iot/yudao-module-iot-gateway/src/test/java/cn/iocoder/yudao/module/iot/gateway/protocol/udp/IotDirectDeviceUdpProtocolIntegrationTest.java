package cn.iocoder.yudao.module.iot.gateway.protocol.udp;

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
import cn.iocoder.yudao.module.iot.gateway.codec.tcp.IotTcpBinaryDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.codec.tcp.IotTcpJsonDeviceMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * IoT 直连设备 UDP 协议集成测试（手动测试）
 *
 * <p>测试场景：直连设备（IotProductDeviceTypeEnum 的 DIRECT 类型）通过 UDP 协议直接连接平台
 *
 * <p>支持两种编解码格式：
 * <ul>
 *     <li>{@link IotTcpJsonDeviceMessageCodec} - JSON 格式</li>
 *     <li>{@link IotTcpBinaryDeviceMessageCodec} - 二进制格式</li>
 * </ul>
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（UDP 端口 8093）</li>
 *     <li>修改 {@link #CODEC} 选择测试的编解码格式</li>
 *     <li>运行 {@link #testAuth()} 获取设备 token，将返回的 token 粘贴到 {@link #TOKEN} 常量</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testPropertyPost()} - 设备属性上报</li>
 *             <li>{@link #testEventPost()} - 设备事件上报</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * <p>注意：UDP 协议是无状态的，每次请求需要在 params 中携带 token（与 HTTP 通过 Header 传递不同）
 *
 * @author 芋道源码
 */
@Slf4j
@Disabled
public class IotDirectDeviceUdpProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8093;
    private static final int TIMEOUT_MS = 5000;

    // ===================== 编解码器选择（修改此处切换 JSON / Binary） =====================

    private static final IotDeviceMessageCodec CODEC = new IotTcpJsonDeviceMessageCodec();
//    private static final IotDeviceMessageCodec CODEC = new IotTcpBinaryDeviceMessageCodec();

    // ===================== 直连设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================

    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String DEVICE_SECRET = "0baa4c2ecc104ae1a26b4070c218bdf3";

    /**
     * 直连设备 Token：从 {@link #testAuth()} 方法获取后，粘贴到这里
     */
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm9kdWN0S2V5IjoiNGF5bVpnT1RPT0NyREtSVCIsImV4cCI6MTc2OTk0ODYzOCwiZGV2aWNlTmFtZSI6InNtYWxsIn0.TrOJisXhloZ3quLBOAIyowmpq6Syp9PHiEpfj-nQ9xo";

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
        log.info("[testAuth][Codec: {}, 请求消息: {}, 数据包长度: {} 字节]", CODEC.type(), request, payload.length);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testAuth][响应消息: {}]", response);
                log.info("[testAuth][请将返回的 token 复制到 TOKEN 常量中]");
            } else {
                log.warn("[testAuth][未收到响应]");
            }
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
        // 1.1 构建注册消息
        IotDeviceRegisterReqDTO registerReqDTO = new IotDeviceRegisterReqDTO();
        registerReqDTO.setProductKey(PRODUCT_KEY);
        registerReqDTO.setDeviceName("test-udp-" + System.currentTimeMillis());
        registerReqDTO.setProductSecret("test-product-secret");
        IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod(), registerReqDTO, null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testDeviceRegister][Codec: {}, 请求消息: {}, 数据包长度: {} 字节]", CODEC.type(), request, payload.length);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testDeviceRegister][响应消息: {}]", response);
                log.info("[testDeviceRegister][成功后可使用返回的 deviceSecret 进行一机一密认证]");
            } else {
                log.warn("[testDeviceRegister][未收到响应]");
            }
        }
    }

    // ===================== 直连设备属性上报测试 =====================

    /**
     * 属性上报测试
     */
    @Test
    public void testPropertyPost() throws Exception {
        // 1.1 构建属性上报消息（UDP 协议：token 放在 params 中）
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                withToken(IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                        .put("width", 1)
                        .put("height", "2")
                        .build())),
                null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testPropertyPost][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testPropertyPost][响应消息: {}]", response);
            } else {
                log.warn("[testPropertyPost][未收到响应]");
            }
        }
    }

    // ===================== 直连设备事件上报测试 =====================

    /**
     * 事件上报测试
     */
    @Test
    public void testEventPost() throws Exception {
        // 1.1 构建事件上报消息（UDP 协议：token 放在 params 中）
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                withToken(IotDeviceEventPostReqDTO.of(
                        "eat",
                        MapUtil.<String, Object>builder().put("rice", 3).build(),
                        System.currentTimeMillis())),
                null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testEventPost][Codec: {}, 请求消息: {}]", CODEC.type(), request);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testEventPost][响应消息: {}]", response);
            } else {
                log.warn("[testEventPost][未收到响应]");
            }
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 构建带 token 的 params
     * <p>
     * 返回格式：{token: "xxx", body: params}
     * - token：JWT 令牌
     * - body：实际请求内容（可以是 Map、List 或其他类型）
     *
     * @param params 原始参数（Map、List 或对象）
     * @return 包含 token 和 body 的 Map
     */
    private Map<String, Object> withToken(Object params) {
        Map<String, Object> result = new HashMap<>();
        result.put("token", TOKEN);
        result.put("body", params);
        return result;
    }

    /**
     * 发送 UDP 请求并接收响应
     *
     * @param socket  UDP Socket
     * @param payload 请求数据
     * @return 响应数据
     */
    public static byte[] sendAndReceive(DatagramSocket socket, byte[] payload) throws Exception {
        InetAddress address = InetAddress.getByName(SERVER_HOST);

        // 发送请求
        DatagramPacket sendPacket = new DatagramPacket(payload, payload.length, address, SERVER_PORT);
        socket.send(sendPacket);

        // 接收响应
        byte[] receiveData = new byte[4096];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            socket.receive(receivePacket);
            byte[] response = new byte[receivePacket.getLength()];
            System.arraycopy(receivePacket.getData(), 0, response, 0, receivePacket.getLength());
            return response;
        } catch (java.net.SocketTimeoutException e) {
            log.warn("[sendAndReceive][接收响应超时]");
            return null;
        }
    }

}
