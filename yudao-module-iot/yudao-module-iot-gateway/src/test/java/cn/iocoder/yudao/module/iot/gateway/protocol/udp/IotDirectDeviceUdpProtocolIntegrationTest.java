package cn.iocoder.yudao.module.iot.gateway.protocol.udp;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * IoT 直连设备 UDP 协议集成测试（手动测试）
 *
 * <p>测试场景：直连设备（IotProductDeviceTypeEnum 的 DIRECT 类型）通过 UDP 协议直接连接平台
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（UDP 端口 8092）</li>
 *     <li>运行 {@link #testDeviceRegister()} 测试直连设备动态注册（一型一密）</li>
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
public class IotDirectDeviceUdpProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8093;
    private static final int TIMEOUT_MS = 5000;

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
                .build());
        // 1.2 输出请求
        log.info("[testAuth][请求体: {}]", payload);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            String response = sendAndReceive(socket, payload);
            // 2.2 输出结果
            log.info("[testAuth][响应体: {}]", response);
            log.info("[testAuth][请将返回的 token 复制到 TOKEN 常量中]");
        }
    }

    // ===================== 直连设备属性上报测试 =====================

    /**
     * 属性上报测试
     */
    @Test
    public void testPropertyPost() throws Exception {
        // 1.1 构建请求（UDP 协议：token 放在 params 中）
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())
                .put("version", "1.0")
                .put("params", withToken(IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                        .put("width", 1)
                        .put("height", "2")
                        .build())))
                .build());
        // 1.2 输出请求
        log.info("[testPropertyPost][请求体: {}]", payload);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            String response = sendAndReceive(socket, payload);
            // 2.2 输出结果
            log.info("[testPropertyPost][响应体: {}]", response);
        }
    }

    // ===================== 直连设备事件上报测试 =====================

    /**
     * 事件上报测试
     */
    @Test
    public void testEventPost() throws Exception {
        // 1.1 构建请求（UDP 协议：token 放在 params 中）
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.EVENT_POST.getMethod())
                .put("version", "1.0")
                .put("params", withToken(IotDeviceEventPostReqDTO.of(
                        "eat",
                        MapUtil.<String, Object>builder().put("rice", 3).build(),
                        System.currentTimeMillis())))
                .build());
        // 1.2 输出请求
        log.info("[testEventPost][请求体: {}]", payload);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            String response = sendAndReceive(socket, payload);
            // 2.2 输出结果
            log.info("[testEventPost][响应体: {}]", response);
        }
    }

    // ===================== 动态注册测试 =====================

    /**
     * 直连设备动态注册测试（一型一密）
     * <p>
     * 使用产品密钥（productSecret）验证身份，成功后返回设备密钥（deviceSecret）
     * <p>
     * 注意：此接口不需要 Token 认证
     */
    @Test
    public void testDeviceRegister() throws Exception {
        // 1.1 构建请求参数
        IotDeviceRegisterReqDTO reqDTO = new IotDeviceRegisterReqDTO();
        reqDTO.setProductKey(PRODUCT_KEY);
        reqDTO.setDeviceName("test-" + System.currentTimeMillis());
        reqDTO.setProductSecret("test-product-secret");
        // 1.2 构建请求
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.DEVICE_REGISTER.getMethod())
                .put("params", reqDTO)
                .build());
        // 1.3 输出请求
        log.info("[testDeviceRegister][请求体: {}]", payload);

        // 2.1 发送请求
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            String response = sendAndReceive(socket, payload);
            // 2.2 输出结果
            log.info("[testDeviceRegister][响应体: {}]", response);
            log.info("[testDeviceRegister][成功后可使用返回的 deviceSecret 进行一机一密认证]");
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 将 token 添加到 params 中
     * <p>
     * 支持 Map 或普通对象，通过 JSON 转换统一处理
     *
     * @param params 原始参数（Map 或对象）
     * @return 添加了 token 的 Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> withToken(Object params) {
        // 1. 转成 Map
        Map<String, Object> map;
        if (params instanceof Map) {
            map = new HashMap<>((Map<String, Object>) params);
        } else {
            // 对象转 Map（通过 JSON 序列化再反序列化）
            map = JsonUtils.parseObject(JsonUtils.toJsonString(params), Map.class);
        }
        // 2. 添加 token
        if (map != null) {
            map.put("token", TOKEN);
        }
        return map;
    }

    /**
     * 发送 UDP 请求并接收响应
     *
     * @param socket  UDP Socket
     * @param payload 请求体
     * @return 响应内容
     */
    private String sendAndReceive(DatagramSocket socket, String payload) throws Exception {
        byte[] sendData = payload.getBytes(StandardCharsets.UTF_8);
        InetAddress address = InetAddress.getByName(SERVER_HOST);

        // 发送请求
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
        socket.send(sendPacket);

        // 接收响应
        byte[] receiveData = new byte[4096];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            socket.receive(receivePacket);
            return new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
        } catch (java.net.SocketTimeoutException e) {
            log.warn("[sendAndReceive][接收响应超时]");
            return null;
        }
    }

}
