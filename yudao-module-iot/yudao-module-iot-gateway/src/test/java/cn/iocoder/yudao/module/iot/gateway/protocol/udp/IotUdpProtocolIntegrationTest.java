package cn.iocoder.yudao.module.iot.gateway.protocol.udp;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * IoT 网关 UDP 协议集成测试（手动测试）
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（UDP 端口 8092）</li>
 *     <li>运行 {@link #testAuth()} 获取 token，将返回的 token 粘贴到 {@link #TOKEN} 常量</li>
 *     <li>运行 {@link #testPropertyPost()} 测试属性上报，或运行 {@link #testEventPost()} 测试事件上报</li>
 * </ol>
 *
 * @author 芋道源码
 */
@Slf4j
public class IotUdpProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8092;
    private static final int TIMEOUT_MS = 5000;

    // 设备信息（根据实际情况修改 PRODUCT_KEY、DEVICE_NAME、PASSWORD）
    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String PASSWORD = "509e2b08f7598eb139d276388c600435913ba4c94cd0d50aebc5c0d1855bcb75";

    private static final String CLIENT_ID = PRODUCT_KEY + "." + DEVICE_NAME;
    private static final String USERNAME = DEVICE_NAME + "&" + PRODUCT_KEY;

    /**
     * 设备 Token：从 {@link #testAuth()} 方法获取后，粘贴到这里
     */
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcm9kdWN0S2V5IjoiNGF5bVpnT1RPT0NyREtSVCIsImV4cCI6MTc2OTMwNTA1NSwiZGV2aWNlTmFtZSI6InNtYWxsIn0.mf3MEATCn5bp6cXgULunZjs8d00RGUxj96JEz0hMS7k";

    /**
     * 认证测试：获取设备 Token
     */
    @Test
    public void testAuth() throws Exception {
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", "auth")
                .put("params", MapUtil.builder()
                        .put("clientId", CLIENT_ID)
                        .put("username", USERNAME)
                        .put("password", PASSWORD)
                        .build())
                .build());

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);

            log.info("[testAuth][请求体: {}]", payload);

            String response = sendAndReceive(socket, payload);

            log.info("[testAuth][响应体: {}]", response);
            log.info("[testAuth][请将返回的 token 复制到 TOKEN 常量中]");
        }
    }

    /**
     * 属性上报测试
     */
    @Test
    public void testPropertyPost() throws Exception {
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())
                .put("version", "1.0")
                .put("params", MapUtil.builder()
                        .put("token", TOKEN)
                        .put("width", 1)
                        .put("height", "2")
                        .build())
                .build());

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);

            log.info("[testPropertyPost][请求体: {}]", payload);

            String response = sendAndReceive(socket, payload);

            log.info("[testPropertyPost][响应体: {}]", response);
        }
    }

    /**
     * 事件上报测试
     */
    @Test
    public void testEventPost() throws Exception {
        String payload = JsonUtils.toJsonString(MapUtil.builder()
                .put("id", IdUtil.fastSimpleUUID())
                .put("method", IotDeviceMessageMethodEnum.EVENT_POST.getMethod())
                .put("version", "1.0")
                .put("identifier", "eat")
                .put("params", MapUtil.builder()
                        .put("token", TOKEN)
                        .put("width", 1)
                        .put("height", "2")
                        .put("oneThree", "3")
                        .build())
                .build());

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);

            log.info("[testEventPost][请求体: {}]", payload);

            String response = sendAndReceive(socket, payload);

            log.info("[testEventPost][响应体: {}]", response);
        }
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
