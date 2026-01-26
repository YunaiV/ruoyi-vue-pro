package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.json;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * IoT 直连设备 TCP JSON 协议集成测试（手动测试）
 *
 * <p>测试场景：直连设备（IotProductDeviceTypeEnum 的 DIRECT 类型）通过 TCP JSON 协议直接连接平台
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（TCP 端口 8091）</li>
 *     <li>运行 {@link #testAuth()} 获取设备认证，认证成功后连接保持</li>
 *     <li>运行以下测试方法：
 *         <ul>
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
public class IotDirectDeviceTcpJsonProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8091;
    private static final int TIMEOUT_MS = 5000;

    // ===================== 直连设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================
    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String DEVICE_SECRET = "0baa4c2ecc104ae1a26b4070c218bdf3";

    // ===================== 认证测试 =====================

    /**
     * 认证测试：设备认证
     */
    @Test
    public void testAuth() throws Exception {
        // 1.1 构建请求
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", "auth")
                .put("params", authReqDTO)
                .put("timestamp", System.currentTimeMillis())
                .build());
        // 1.2 输出请求
        log.info("[testAuth][请求体: {}]", payload);

        // 2.1 发送请求
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);
            String response = sendAndReceive(socket, payload);
            // 2.2 输出结果
            log.info("[testAuth][响应体: {}]", response);
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
            String authResponse = authenticate(socket);
            log.info("[testPropertyPost][认证响应: {}]", authResponse);

            // 2.1 构建属性上报请求
            String payload = JsonUtils.toJsonString(MapUtil.builder()
                    .put("id", IdUtil.fastSimpleUUID())
                    .put("method", IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())
                    .put("params", IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                            .put("width", 1)
                            .put("height", "2")
                            .build()))
                    .put("timestamp", System.currentTimeMillis())
                    .build());
            // 2.2 输出请求
            log.info("[testPropertyPost][请求体: {}]", payload);

            // 3.1 发送请求
            String response = sendAndReceive(socket, payload);
            // 3.2 输出结果
            log.info("[testPropertyPost][响应体: {}]", response);
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
            String authResponse = authenticate(socket);
            log.info("[testEventPost][认证响应: {}]", authResponse);

            // 2.1 构建事件上报请求
            String payload = JsonUtils.toJsonString(MapUtil.builder()
                    .put("id", IdUtil.fastSimpleUUID())
                    .put("method", IotDeviceMessageMethodEnum.EVENT_POST.getMethod())
                    .put("params", IotDeviceEventPostReqDTO.of(
                            "eat",
                            MapUtil.<String, Object>builder().put("rice", 3).build(),
                            System.currentTimeMillis()))
                    .put("timestamp", System.currentTimeMillis())
                    .build());
            // 2.2 输出请求
            log.info("[testEventPost][请求体: {}]", payload);

            // 3.1 发送请求
            String response = sendAndReceive(socket, payload);
            // 3.2 输出结果
            log.info("[testEventPost][响应体: {}]", response);
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 执行设备认证
     *
     * @param socket TCP 连接
     * @return 认证响应
     */
    private String authenticate(Socket socket) throws Exception {
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", "auth")
                .put("params", authReqDTO)
                .put("timestamp", System.currentTimeMillis())
                .build());
        return sendAndReceive(socket, payload);
    }

    /**
     * 发送 TCP 请求并接收响应
     *
     * @param socket  TCP Socket
     * @param payload 请求体
     * @return 响应内容
     */
    private String sendAndReceive(Socket socket, String payload) throws Exception {
        // 1. 发送请求
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        byte[] sendData = payload.getBytes(StandardCharsets.UTF_8);
        out.write(sendData);
        out.flush();

        // 2.1 等待一小段时间让服务器处理
        Thread.sleep(100);
        // 2.2 接收响应
        byte[] buffer = new byte[4096];
        try {
            int length = in.read(buffer);
            if (length > 0) {
                return new String(buffer, 0, length, StandardCharsets.UTF_8);
            }
            return null;
        } catch (java.net.SocketTimeoutException e) {
            log.warn("[sendAndReceive][接收响应超时]");
            return null;
        }
    }

}
