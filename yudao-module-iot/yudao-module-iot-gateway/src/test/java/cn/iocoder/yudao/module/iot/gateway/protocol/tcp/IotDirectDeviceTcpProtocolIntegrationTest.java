package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.HexUtil;
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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * IoT 直连设备 TCP 协议集成测试（手动测试）
 *
 * <p>测试场景：直连设备（IotProductDeviceTypeEnum 的 DIRECT 类型）通过 TCP 协议直接连接平台
 *
 * <p>支持两种编解码格式：
 * <ul>
 *     <li>{@link IotTcpJsonDeviceMessageCodec} - JSON 格式</li>
 *     <li>{@link IotTcpBinaryDeviceMessageCodec} - 二进制格式</li>
 * </ul>
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（TCP 端口 8091）</li>
 *     <li>修改 {@link #CODEC} 选择测试的编解码格式</li>
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

    // ===================== 编解码器选择（修改此处切换 JSON / Binary） =====================

//    private static final IotDeviceMessageCodec CODEC = new IotTcpJsonDeviceMessageCodec();
    private static final IotDeviceMessageCodec CODEC = new IotTcpBinaryDeviceMessageCodec();

    // ===================== 直连设备信息（根据实际情况修改，从 iot_device 表查询） =====================

    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String DEVICE_SECRET = "0baa4c2ecc104ae1a26b4070c218bdf3";

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
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 解码响应
            if (responseBytes != null) {
                IotDeviceMessage response = CODEC.decode(responseBytes);
                log.info("[testAuth][响应消息: {}]", response);
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
        registerReqDTO.setDeviceName("test-tcp-" + System.currentTimeMillis());
        registerReqDTO.setProductSecret("test-product-secret");
        IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod(), registerReqDTO, null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testDeviceRegister][Codec: {}, 请求消息: {}, 数据包长度: {} 字节]", CODEC.type(), request, payload.length);

        // 2.1 发送请求
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
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
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);

            // 1. 先进行认证
            IotDeviceMessage authResponse = authenticate(socket);
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
            log.info("[testPropertyPost][Codec: {}, 请求消息: {}]", CODEC.type(), request);

            // 3.1 发送请求
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 3.2 解码响应
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
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);

            // 1. 先进行认证
            IotDeviceMessage authResponse = authenticate(socket);
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
            log.info("[testEventPost][Codec: {}, 请求消息: {}]", CODEC.type(), request);

            // 3.1 发送请求
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 3.2 解码响应
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
     * 执行设备认证
     *
     * @param socket TCP 连接
     * @return 认证响应消息
     */
    private IotDeviceMessage authenticate(Socket socket) throws Exception {
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        IotDeviceMessage request = IotDeviceMessage.of(IdUtil.fastSimpleUUID(), "auth", authReqDTO, null, null, null);
        byte[] payload = CODEC.encode(request);
        byte[] responseBytes = sendAndReceive(socket, payload);
        if (responseBytes != null) {
            log.info("[authenticate][响应数据长度: {} 字节，首字节: 0x{}, HEX: {}]",
                    responseBytes.length,
                    String.format("%02X", responseBytes[0]),
                    HexUtil.encodeHexStr(responseBytes));
            return CODEC.decode(responseBytes);
        }
        return null;
    }

    /**
     * 发送 TCP 请求并接收响应
     *
     * @param socket  TCP Socket
     * @param payload 请求数据
     * @return 响应数据
     */
    private byte[] sendAndReceive(Socket socket, byte[] payload) throws Exception {
        // 1. 发送请求
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        out.write(payload);
        out.flush();

        // 2.1 等待一小段时间让服务器处理
        Thread.sleep(100);
        // 2.2 接收响应
        byte[] buffer = new byte[4096];
        try {
            int length = in.read(buffer);
            if (length > 0) {
                byte[] response = new byte[length];
                System.arraycopy(buffer, 0, response, 0, length);
                return response;
            }
            return null;
        } catch (java.net.SocketTimeoutException e) {
            log.warn("[sendAndReceive][接收响应超时]");
            return null;
        }
    }

}
