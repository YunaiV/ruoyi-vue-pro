package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.binary;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.property.IotDevicePropertyPostReqDTO;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceAuthUtils;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * IoT 直连设备 TCP 二进制协议集成测试（手动测试）
 *
 * <p>测试场景：直连设备（IotProductDeviceTypeEnum 的 DIRECT 类型）通过 TCP 二进制协议直接连接平台
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
 * <p>二进制协议格式说明请参考：{@code tcp-binary-packet-examples.md}
 *
 * @author 芋道源码
 */
@Slf4j
public class IotDirectDeviceTcpBinaryProtocolIntegrationTest {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8091;
    private static final int TIMEOUT_MS = 5000;

    // ===================== 二进制协议常量 =====================
    /**
     * 协议魔术字，用于协议识别
     */
    private static final byte MAGIC_NUMBER = (byte) 0x7E;

    /**
     * 协议版本号
     */
    private static final byte PROTOCOL_VERSION = (byte) 0x01;

    /**
     * 请求消息类型
     */
    private static final byte REQUEST = (byte) 0x01;

    // ===================== 直连设备信息（根据实际情况修改，从 iot_device 表查询子设备） =====================
    private static final String PRODUCT_KEY = "4aymZgOTOOCrDKRT";
    private static final String DEVICE_NAME = "small";
    private static final String DEVICE_SECRET = "0baa4c2ecc104ae1a26b4070c218bdf3";

    // ===================== 认证测试 =====================

    /**
     * 认证测试：设备认证（二进制格式）
     */
    @Test
    public void testAuth() throws Exception {
        // 1.1 构建认证参数
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        // 1.2 构建二进制请求
        String messageId = IdUtil.fastSimpleUUID();
        String method = "auth";
        byte[] payload = buildBinaryRequest(messageId, method, authReqDTO);
        // 1.3 输出请求
        log.info("[testAuth][消息ID: {}, 方法: {}, 参数: {}]", messageId, method, JsonUtils.toJsonString(authReqDTO));
        log.info("[testAuth][二进制数据包长度: {} 字节]", payload.length);
        log.info("[testAuth][二进制数据包(HEX): {}]", bytesToHex(payload));

        // 2.1 发送请求
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);
            byte[] response = sendAndReceiveBinary(socket, payload);
            // 2.2 输出结果
            if (response != null) {
                log.info("[testAuth][响应数据包长度: {} 字节]", response.length);
                log.info("[testAuth][响应数据包(HEX): {}]", bytesToHex(response));
                parseBinaryResponse(response);
            } else {
                log.warn("[testAuth][未收到响应]");
            }
        }
    }

    // ===================== 直连设备属性上报测试 =====================

    /**
     * 属性上报测试（二进制格式）
     *
     * 注意：TCP 协议需要先认证，这里为了简化测试，在同一连接上先认证再上报
     */
    @Test
    public void testPropertyPost() throws Exception {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);

            // 1. 先进行认证
            byte[] authResponse = authenticateBinary(socket);
            log.info("[testPropertyPost][认证响应长度: {} 字节]", authResponse != null ? authResponse.length : 0);
            if (authResponse != null) {
                parseBinaryResponse(authResponse);
            }

            // 2.1 构建属性上报请求
            String messageId = IdUtil.fastSimpleUUID();
            String method = IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod();
            Object params = IotDevicePropertyPostReqDTO.of(MapUtil.<String, Object>builder()
                    .put("width", 1)
                    .put("height", "2")
                    .build());
            byte[] payload = buildBinaryRequest(messageId, method, params);
            // 2.2 输出请求
            log.info("[testPropertyPost][消息ID: {}, 方法: {}, 参数: {}]", messageId, method, JsonUtils.toJsonString(params));
            log.info("[testPropertyPost][二进制数据包长度: {} 字节]", payload.length);

            // 3.1 发送请求
            byte[] response = sendAndReceiveBinary(socket, payload);
            // 3.2 输出结果
            if (response != null) {
                log.info("[testPropertyPost][响应数据包长度: {} 字节]", response.length);
                parseBinaryResponse(response);
            } else {
                log.warn("[testPropertyPost][未收到响应]");
            }
        }
    }

    // ===================== 直连设备事件上报测试 =====================

    /**
     * 事件上报测试（二进制格式）
     *
     * 注意：TCP 协议需要先认证，这里为了简化测试，在同一连接上先认证再上报
     */
    @Test
    public void testEventPost() throws Exception {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            socket.setSoTimeout(TIMEOUT_MS);

            // 1. 先进行认证
            byte[] authResponse = authenticateBinary(socket);
            log.info("[testEventPost][认证响应长度: {} 字节]", authResponse != null ? authResponse.length : 0);
            if (authResponse != null) {
                parseBinaryResponse(authResponse);
            }

            // 2.1 构建事件上报请求
            String messageId = IdUtil.fastSimpleUUID();
            String method = IotDeviceMessageMethodEnum.EVENT_POST.getMethod();
            Object params = IotDeviceEventPostReqDTO.of(
                    "eat",
                    MapUtil.<String, Object>builder().put("rice", 3).build(),
                    System.currentTimeMillis());
            byte[] payload = buildBinaryRequest(messageId, method, params);
            // 2.2 输出请求
            log.info("[testEventPost][消息ID: {}, 方法: {}, 参数: {}]", messageId, method, JsonUtils.toJsonString(params));
            log.info("[testEventPost][二进制数据包长度: {} 字节]", payload.length);

            // 3.1 发送请求
            byte[] response = sendAndReceiveBinary(socket, payload);
            // 3.2 输出结果
            if (response != null) {
                log.info("[testEventPost][响应数据包长度: {} 字节]", response.length);
                parseBinaryResponse(response);
            } else {
                log.warn("[testEventPost][未收到响应]");
            }
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 执行设备认证（二进制格式）
     *
     * @param socket TCP 连接
     * @return 认证响应
     */
    private byte[] authenticateBinary(Socket socket) throws Exception {
        IotDeviceAuthReqDTO authInfo = IotDeviceAuthUtils.getAuthInfo(PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET);
        IotDeviceAuthReqDTO authReqDTO = new IotDeviceAuthReqDTO()
                .setClientId(authInfo.getClientId())
                .setUsername(authInfo.getUsername())
                .setPassword(authInfo.getPassword());
        String messageId = IdUtil.fastSimpleUUID();
        byte[] payload = buildBinaryRequest(messageId, "auth", authReqDTO);
        return sendAndReceiveBinary(socket, payload);
    }

    /**
     * 构建二进制请求数据包
     *
     * <p>协议格式：
     * <pre>
     * +--------+--------+--------+---------------------------+
     * | 魔术字 | 版本号 | 消息类型|      消息长度(4字节)      |
     * +--------+--------+--------+---------------------------+
     * |    消息ID长度(2字节)    |    消息ID(变长字符串)      |
     * +--------+--------+--------+--------+--------+--------+
     * |    方法名长度(2字节)    |    方法名(变长字符串)      |
     * +--------+--------+--------+--------+--------+--------+
     * |                 消息体数据(变长)                    |
     * +--------+--------+--------+--------+--------+--------+
     * </pre>
     *
     * @param messageId 消息 ID
     * @param method    方法名
     * @param params    请求参数
     * @return 二进制数据包
     */
    private byte[] buildBinaryRequest(String messageId, String method, Object params) {
        Buffer buffer = Buffer.buffer();
        // 1. 写入协议头部
        buffer.appendByte(MAGIC_NUMBER);
        buffer.appendByte(PROTOCOL_VERSION);
        buffer.appendByte(REQUEST);
        // 2. 预留消息长度位置
        int lengthPosition = buffer.length();
        buffer.appendInt(0);
        // 3. 写入消息 ID
        byte[] messageIdBytes = StrUtil.utf8Bytes(messageId);
        buffer.appendShort((short) messageIdBytes.length);
        buffer.appendBytes(messageIdBytes);
        // 4. 写入方法名
        byte[] methodBytes = StrUtil.utf8Bytes(method);
        buffer.appendShort((short) methodBytes.length);
        buffer.appendBytes(methodBytes);
        // 5. 写入消息体（params 序列化为 JSON）
        if (params != null) {
            buffer.appendBytes(JsonUtils.toJsonByte(params));
        }
        // 6. 更新消息长度
        buffer.setInt(lengthPosition, buffer.length());
        return buffer.getBytes();
    }

    /**
     * 解析二进制响应数据包
     *
     * @param data 响应数据
     */
    private void parseBinaryResponse(byte[] data) {
        if (data == null || data.length < 11) {
            log.warn("[parseBinaryResponse][数据包过短]");
            return;
        }

        Buffer buffer = Buffer.buffer(data);
        int index = 0;

        // 1. 解析魔术字
        byte magic = buffer.getByte(index++);
        log.info("[parseBinaryResponse][魔术字: 0x{} (预期: 0x7E)]", String.format("%02X", magic));

        // 2. 解析版本号
        byte version = buffer.getByte(index++);
        log.info("[parseBinaryResponse][版本号: 0x{}]", String.format("%02X", version));

        // 3. 解析消息类型
        byte messageType = buffer.getByte(index++);
        log.info("[parseBinaryResponse][消息类型: 0x{} (0x01=请求, 0x02=响应)]", String.format("%02X", messageType));

        // 4. 解析消息长度
        int messageLength = buffer.getInt(index);
        index += 4;
        log.info("[parseBinaryResponse][消息长度: {}]", messageLength);

        // 5. 解析消息 ID
        short messageIdLength = buffer.getShort(index);
        index += 2;
        String messageId = buffer.getString(index, index + messageIdLength, StandardCharsets.UTF_8.name());
        index += messageIdLength;
        log.info("[parseBinaryResponse][消息ID: {}]", messageId);

        // 6. 解析方法名
        short methodLength = buffer.getShort(index);
        index += 2;
        String method = buffer.getString(index, index + methodLength, StandardCharsets.UTF_8.name());
        index += methodLength;
        log.info("[parseBinaryResponse][方法名: {}]", method);

        // 7. 解析消息体
        if (messageType == 0x02) { // RESPONSE
            // 响应消息：code(4) + msgLen(2) + msg + data(JSON)
            if (index + 4 <= buffer.length()) {
                int code = buffer.getInt(index);
                index += 4;
                log.info("[parseBinaryResponse][响应码: {}]", code);

                if (index + 2 <= buffer.length()) {
                    short msgLength = buffer.getShort(index);
                    index += 2;
                    if (msgLength > 0 && index + msgLength <= buffer.length()) {
                        String msg = buffer.getString(index, index + msgLength, StandardCharsets.UTF_8.name());
                        index += msgLength;
                        log.info("[parseBinaryResponse][响应消息: {}]", msg);
                    }

                    if (index < buffer.length()) {
                        String dataJson = buffer.getString(index, buffer.length(), StandardCharsets.UTF_8.name());
                        log.info("[parseBinaryResponse][响应数据: {}]", dataJson);
                    }
                }
            }
        } else {
            // 请求消息：params(JSON)
            if (index < buffer.length()) {
                String paramsJson = buffer.getString(index, buffer.length(), StandardCharsets.UTF_8.name());
                log.info("[parseBinaryResponse][请求参数: {}]", paramsJson);
            }
        }
    }

    /**
     * 发送二进制请求并接收响应
     *
     * @param socket  TCP Socket
     * @param payload 二进制数据包
     * @return 响应数据
     */
    private byte[] sendAndReceiveBinary(Socket socket, byte[] payload) throws Exception {
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
            log.warn("[sendAndReceiveBinary][接收响应超时]");
            return null;
        }
    }

    // TODO @AI：hutool 简化下；
    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

}
