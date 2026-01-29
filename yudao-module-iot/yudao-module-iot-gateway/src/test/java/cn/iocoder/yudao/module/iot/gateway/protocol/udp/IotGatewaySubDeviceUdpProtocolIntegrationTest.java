package cn.iocoder.yudao.module.iot.gateway.protocol.udp;

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

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.module.iot.gateway.protocol.udp.IotDirectDeviceUdpProtocolIntegrationTest.sendAndReceive;

/**
 * IoT 网关子设备 UDP 协议集成测试（手动测试）
 *
 * <p>测试场景：子设备（IotProductDeviceTypeEnum 的 SUB 类型）通过网关设备代理上报数据
 *
 * <p><b>重要说明：子设备无法直接连接平台，所有请求均由网关设备（Gateway）代为转发。</b>
 * <p>网关设备转发子设备请求时，Token 使用子设备自己的信息。
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
 *     <li>确保子设备已通过 {@link IotGatewayDeviceUdpProtocolIntegrationTest#testTopoAdd()} 绑定到网关</li>
 *     <li>修改 {@link #CODEC} 选择测试的编解码格式</li>
 *     <li>运行 {@link #testAuth()} 获取子设备 token，将返回的 token 粘贴到 {@link #TOKEN} 常量</li>
 *     <li>运行以下测试方法：
 *         <ul>
 *             <li>{@link #testPropertyPost()} - 子设备属性上报（由网关代理转发）</li>
 *             <li>{@link #testEventPost()} - 子设备事件上报（由网关代理转发）</li>
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
public class IotGatewaySubDeviceUdpProtocolIntegrationTest {

    private static final int TIMEOUT_MS = 5000;

    // ===================== 编解码器选择（修改此处切换 JSON / Binary） =====================

    private static final IotDeviceMessageCodec CODEC = new IotTcpJsonDeviceMessageCodec();
//    private static final IotDeviceMessageCodec CODEC = new IotTcpBinaryDeviceMessageCodec();

    // ===================== 网关子设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================

    private static final String PRODUCT_KEY = "jAufEMTF1W6wnPhn";
    private static final String DEVICE_NAME = "chazuo-it";
    private static final String DEVICE_SECRET = "d46ef9b28ab14238b9c00a3a668032af";

    /**
     * 网关子设备 Token：从 {@link #testAuth()} 方法获取后，粘贴到这里
     */
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm9kdWN0S2V5IjoiakF1ZkVNVEYxVzZ3blBobiIsImV4cCI6MTc2OTk1NDY3OSwiZGV2aWNlTmFtZSI6ImNoYXp1by1pdCJ9.jfbUAoU0xkJl4UvO-NUvcJ6yITPRgUjQ4MKATPuwneg";

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

    // ===================== 子设备属性上报测试 =====================

    /**
     * 子设备属性上报测试
     */
    @Test
    public void testPropertyPost() throws Exception {
        // 1.1 构建属性上报消息（UDP 协议：token 放在 params 中）
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                withToken(IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                        .put("power", 100)
                        .put("status", "online")
                        .put("temperature", 36.5)
                        .build())),
                null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testPropertyPost][子设备属性上报 - 请求实际由 Gateway 代为转发]");
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

    // ===================== 子设备事件上报测试 =====================

    /**
     * 子设备事件上报测试
     */
    @Test
    public void testEventPost() throws Exception {
        // 1.1 构建事件上报消息（UDP 协议：token 放在 params 中）
        IotDeviceMessage request = IotDeviceMessage.of(
                IdUtil.fastSimpleUUID(),
                IotDeviceMessageMethodEnum.EVENT_POST.getMethod(),
                withToken(IotDeviceEventPostReqDTO.of(
                        "alarm",
                        MapUtil.<String, Object>builder()
                                .put("level", "warning")
                                .put("message", "temperature too high")
                                .put("threshold", 40)
                                .put("current", 42)
                                .build(),
                        System.currentTimeMillis())),
                null, null, null);
        // 1.2 编码
        byte[] payload = CODEC.encode(request);
        log.info("[testEventPost][子设备事件上报 - 请求实际由 Gateway 代为转发]");
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

}
