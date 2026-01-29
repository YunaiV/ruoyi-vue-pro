package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
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
 * IoT 网关子设备 TCP 协议集成测试（手动测试）
 *
 * <p>测试场景：子设备（IotProductDeviceTypeEnum 的 SUB 类型）通过网关设备代理上报数据
 *
 * <p><b>重要说明：子设备无法直接连接平台，所有请求均由网关设备（Gateway）代为转发。</b>
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
 *     <li>确保子设备已通过 {@link IotGatewayDeviceTcpProtocolIntegrationTest#testTopoAdd()} 绑定到网关</li>
 *     <li>修改 {@link #CODEC} 选择测试的编解码格式</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testAuth()} - 子设备认证</li>
 *             <li>{@link #testPropertyPost()} - 子设备属性上报（由网关代理转发）</li>
 *             <li>{@link #testEventPost()} - 子设备事件上报（由网关代理转发）</li>
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
public class IotGatewaySubDeviceTcpProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8091;
    private static final int TIMEOUT_MS = 5000;

    // ===================== 编解码器选择（修改此处切换 JSON / Binary） =====================

    private static final IotDeviceMessageCodec CODEC = new IotTcpJsonDeviceMessageCodec();
//    private static final IotDeviceMessageCodec CODEC = new IotTcpBinaryDeviceMessageCodec();

    // ===================== 网关子设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================

    private static final String PRODUCT_KEY = "jAufEMTF1W6wnPhn";
    private static final String DEVICE_NAME = "chazuo-it";
    private static final String DEVICE_SECRET = "d46ef9b28ab14238b9c00a3a668032af";

    // ===================== 认证测试 =====================

    /**
     * 子设备认证测试：获取子设备 Token
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

    // ===================== 子设备属性上报测试 =====================

    /**
     * 子设备属性上报测试
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
                            .put("power", 100)
                            .put("status", "online")
                            .put("temperature", 36.5)
                            .build()),
                    null, null, null);
            // 2.2 编码
            byte[] payload = CODEC.encode(request);
            log.info("[testPropertyPost][子设备属性上报 - 请求实际由 Gateway 代为转发]");
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

    // ===================== 子设备事件上报测试 =====================

    /**
     * 子设备事件上报测试
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
                            "alarm",
                            MapUtil.<String, Object>builder()
                                    .put("level", "warning")
                                    .put("message", "temperature too high")
                                    .put("threshold", 40)
                                    .put("current", 42)
                                    .build(),
                            System.currentTimeMillis()),
                    null, null, null);
            // 2.2 编码
            byte[] payload = CODEC.encode(request);
            log.info("[testEventPost][子设备事件上报 - 请求实际由 Gateway 代为转发]");
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
     * 执行子设备认证
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
