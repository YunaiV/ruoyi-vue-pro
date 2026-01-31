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
import cn.iocoder.yudao.module.iot.gateway.serialize.IotMessageSerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.binary.IotBinarySerializer;
import cn.iocoder.yudao.module.iot.gateway.serialize.json.IotJsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * IoT 直连设备 TCP 协议集成测试（手动测试）
 *
 * <p>测试场景：直连设备（IotProductDeviceTypeEnum 的 DIRECT 类型）通过 TCP 协议直接连接平台
 *
 * <p>支持两种序列化格式：
 * <ul>
 *     <li>{@link IotJsonSerializer} - JSON 格式</li>
 *     <li>{@link IotBinarySerializer} - 二进制格式</li>
 * </ul>
 *
 * <p>TCP 拆包配置（需与 application.yaml 中的 codec 配置一致）：
 * <ul>
 *     <li>type: delimiter - 基于分隔符拆包</li>
 *     <li>delimiter: \n - 换行符作为分隔符</li>
 * </ul>
 *
 * <p>使用步骤：
 * <ol>
 *     <li>启动 yudao-module-iot-gateway 服务（TCP 端口 8091）</li>
 *     <li>修改 {@link #SERIALIZER} 选择测试的序列化格式（Delimiter 模式只支持 JSON）</li>
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

    // TODO @AI：这里可以通过 /Users/yunai/Java/ruoyi-vue-pro-jdk25/yudao-module-iot/yudao-module-iot-gateway/src/main/java/cn/iocoder/yudao/module/iot/gateway/protocol/tcp/codec 么？例如说：使用 vertx vertx tcp client？？？从而更好的复用解码逻辑；
    /**
     * 分隔符（需与 application.yaml 中的 delimiter 配置一致）
     */
    private static final String DELIMITER = "\n";

    // ===================== 序列化器选择（Delimiter 模式推荐使用 JSON） =====================

    private static final IotMessageSerializer SERIALIZER = new IotJsonSerializer();
//    private static final IotMessageSerializer SERIALIZER = new IotBinarySerializer();

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
        IotDeviceMessage request = IotDeviceMessage.requestOf("auth", authReqDTO);
        // 1.2 序列化
        // TODO @AI：是不是把 SERIALIZER 放到 sendAndReceive 里；
        byte[] payload = SERIALIZER.serialize(request);
        log.info("[testAuth][Serializer: {}, 请求消息: {}, 数据包长度: {} 字节]", SERIALIZER.getType(), request, payload.length);

        // 2.1 发送请求
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 反序列化响应
            if (responseBytes != null) {
                IotDeviceMessage response = SERIALIZER.deserialize(responseBytes);
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
        // 1.2 序列化
        byte[] payload = SERIALIZER.serialize(request);
        log.info("[testDeviceRegister][Serializer: {}, 请求消息: {}, 数据包长度: {} 字节]", SERIALIZER.getType(), request, payload.length);

        // 2.1 发送请求
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 2.2 反序列化响应
            if (responseBytes != null) {
                IotDeviceMessage response = SERIALIZER.deserialize(responseBytes);
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
            // 2.2 序列化
            byte[] payload = SERIALIZER.serialize(request);
            log.info("[testPropertyPost][Serializer: {}, 请求消息: {}]", SERIALIZER.getType(), request);

            // 3.1 发送请求
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 3.2 反序列化响应
            if (responseBytes != null) {
                IotDeviceMessage response = SERIALIZER.deserialize(responseBytes);
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
            // 2.2 序列化
            byte[] payload = SERIALIZER.serialize(request);
            log.info("[testEventPost][Serializer: {}, 请求消息: {}]", SERIALIZER.getType(), request);

            // 3.1 发送请求
            byte[] responseBytes = sendAndReceive(socket, payload);
            // 3.2 反序列化响应
            if (responseBytes != null) {
                IotDeviceMessage response = SERIALIZER.deserialize(responseBytes);
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
        byte[] payload = SERIALIZER.serialize(request);
        byte[] responseBytes = sendAndReceive(socket, payload);
        if (responseBytes != null) {
            log.info("[authenticate][响应数据长度: {} 字节，首字节: 0x{}, HEX: {}]",
                    responseBytes.length,
                    String.format("%02X", responseBytes[0]),
                    HexUtil.encodeHexStr(responseBytes));
            return SERIALIZER.deserialize(responseBytes);
        }
        return null;
    }

    /**
     * 发送 TCP 请求并接收响应（支持 Delimiter 分隔符协议）
     * <p>
     * 发送格式：[消息体][分隔符]
     * 接收格式：[消息体][分隔符]
     *
     * @param socket  TCP Socket
     * @param payload 请求数据（消息体，不含分隔符）
     * @return 响应数据（消息体，不含分隔符）
     */
    private byte[] sendAndReceive(Socket socket, byte[] payload) throws Exception {
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        // 1. 发送请求（添加分隔符后缀）
        out.write(payload);
        out.write(DELIMITER.getBytes(StandardCharsets.UTF_8));
        out.flush();
        log.info("[sendAndReceive][发送数据: {} 字节（不含分隔符）]", payload.length);

        // 2. 接收响应（读取到分隔符为止）
        try {
            String responseLine = in.readLine();
            if (responseLine != null) {
                byte[] response = responseLine.getBytes(StandardCharsets.UTF_8);
                log.info("[sendAndReceive][接收数据: {} 字节]", response.length);
                return response;
            }
            return null;
        } catch (java.net.SocketTimeoutException e) {
            log.warn("[sendAndReceive][接收响应超时]");
            return null;
        }
    }

}
