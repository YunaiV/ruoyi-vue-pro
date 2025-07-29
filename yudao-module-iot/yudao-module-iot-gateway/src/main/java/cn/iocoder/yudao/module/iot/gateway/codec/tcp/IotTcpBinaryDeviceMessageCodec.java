package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * TCP 二进制格式 {@link IotDeviceMessage} 编解码器
 * <p>
 * 二进制协议格式（所有数值使用大端序）：
 *
 * <pre>
 * +--------+--------+--------+--------+--------+--------+--------+--------+
 * | 魔术字 | 版本号 | 消息类型| 消息标志|         消息长度(4字节)          |
 * +--------+--------+--------+--------+--------+--------+--------+--------+
 * |           消息 ID 长度(2字节)        |      消息 ID (变长字符串)         |
 * +--------+--------+--------+--------+--------+--------+--------+--------+
 * |           方法名长度(2字节)        |      方法名(变长字符串)         |
 * +--------+--------+--------+--------+--------+--------+--------+--------+
 * |                        消息体数据(变长)                              |
 * +--------+--------+--------+--------+--------+--------+--------+--------+
 * </pre>
 * <p>
 * 消息体格式：
 * - 请求消息：params 数据(JSON)
 * - 响应消息：code (4字节) + msg 长度(2字节) + msg 字符串 + data 数据(JSON)
 * <p>
 * 注意：deviceId 不包含在协议中，由服务器根据连接上下文自动设置
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class IotTcpBinaryDeviceMessageCodec implements IotDeviceMessageCodec {

    public static final String TYPE = "TCP_BINARY";

    // ==================== 协议常量 ====================

    /**
     * 协议魔术字，用于协议识别
     */
    private static final byte MAGIC_NUMBER = (byte) 0x7E;

    /**
     * 协议版本号
     */
    private static final byte PROTOCOL_VERSION = (byte) 0x01;

    /**
     * 消息类型常量
     */
    public static class MessageType {
        public static final byte REQUEST = 0x01; // 请求消息
        public static final byte RESPONSE = 0x02; // 响应消息
    }

    /**
     * 协议头部固定长度（魔术字 + 版本号 + 消息类型 + 消息标志 + 消息长度）
     */
    private static final int HEADER_FIXED_LENGTH = 8;

    /**
     * 最小消息长度（头部 + 消息ID长度 + 方法名长度）
     */
    private static final int MIN_MESSAGE_LENGTH = HEADER_FIXED_LENGTH + 4;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public byte[] encode(IotDeviceMessage message) {
        Assert.notNull(message, "消息不能为空");
        Assert.notBlank(message.getMethod(), "消息方法不能为空");

        try {
            // 1. 确定消息类型
            byte messageType = determineMessageType(message);

            // 2. 构建消息体
            byte[] bodyData = buildMessageBody(message, messageType);

            // 3. 构建完整消息（不包含deviceId，由连接上下文管理）
            return buildCompleteMessage(message, messageType, bodyData);

        } catch (Exception e) {
            log.error("[encode][TCP 二进制消息编码失败，消息: {}]", message, e);
            throw new RuntimeException("TCP 二进制消息编码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public IotDeviceMessage decode(byte[] bytes) {
        Assert.notNull(bytes, "待解码数据不能为空");
        Assert.isTrue(bytes.length >= MIN_MESSAGE_LENGTH, "数据包长度不足");

        try {
            Buffer buffer = Buffer.buffer(bytes);

            // 1. 解析协议头部
            ProtocolHeader header = parseProtocolHeader(buffer);

            // 2. 解析消息内容（不包含deviceId，由上层连接管理器设置）
            return parseMessageContent(buffer, header);

        } catch (Exception e) {
            log.error("[decode][TCP 二进制消息解码失败，数据长度: {}]", bytes.length, e);
            throw new RuntimeException("TCP 二进制消息解码失败: " + e.getMessage(), e);
        }
    }

    // ==================== 编码相关方法 ====================

    /**
     * 确定消息类型
     * 优化后的判断逻辑：有响应字段就是响应消息，否则就是请求消息
     */
    private byte determineMessageType(IotDeviceMessage message) {
        // 判断是否为响应消息：有响应码或响应消息时为响应
        if (message.getCode() != null || StrUtil.isNotBlank(message.getMsg())) {
            return MessageType.RESPONSE;
        }
        // 默认为请求消息
        return MessageType.REQUEST;
    }

    /**
     * 构建消息体
     */
    private byte[] buildMessageBody(IotDeviceMessage message, byte messageType) {
        Buffer bodyBuffer = Buffer.buffer();

        if (messageType == MessageType.RESPONSE) {
            // 响应消息：code + msg长度 + msg + data
            bodyBuffer.appendInt(message.getCode() != null ? message.getCode() : 0);

            String msg = message.getMsg() != null ? message.getMsg() : "";
            byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
            bodyBuffer.appendShort((short) msgBytes.length);
            bodyBuffer.appendBytes(msgBytes);

            if (message.getData() != null) {
                bodyBuffer.appendBytes(JsonUtils.toJsonByte(message.getData()));
            }
        } else {
            // 请求消息：包含 params 或 data
            Object payload = message.getParams() != null ? message.getParams() : message.getData();
            if (payload != null) {
                bodyBuffer.appendBytes(JsonUtils.toJsonByte(payload));
            }
        }

        return bodyBuffer.getBytes();
    }

    /**
     * 构建完整消息
     */
    private byte[] buildCompleteMessage(IotDeviceMessage message, byte messageType, byte[] bodyData) {
        Buffer buffer = Buffer.buffer();

        // 1. 写入协议头部
        buffer.appendByte(MAGIC_NUMBER);
        buffer.appendByte(PROTOCOL_VERSION);
        buffer.appendByte(messageType);
        buffer.appendByte((byte) 0x00); // 消息标志，预留字段

        // 2. 预留消息长度位置
        int lengthPosition = buffer.length();
        buffer.appendInt(0);

        // 3. 写入消息ID
        String messageId = StrUtil.isNotBlank(message.getRequestId()) ? message.getRequestId()
                : generateMessageId(message.getMethod());
        byte[] messageIdBytes = messageId.getBytes(StandardCharsets.UTF_8);
        buffer.appendShort((short) messageIdBytes.length);
        buffer.appendBytes(messageIdBytes);

        // 4. 写入方法名
        byte[] methodBytes = message.getMethod().getBytes(StandardCharsets.UTF_8);
        buffer.appendShort((short) methodBytes.length);
        buffer.appendBytes(methodBytes);

        // 5. 写入消息体
        buffer.appendBytes(bodyData);

        // 6. 更新消息长度
        buffer.setInt(lengthPosition, buffer.length());

        return buffer.getBytes();
    }

    /**
     * 生成消息 ID
     */
    private String generateMessageId(String method) {
        return method + "_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }

    // ==================== 解码相关方法 ====================

    /**
     * 解析协议头部
     */
    private ProtocolHeader parseProtocolHeader(Buffer buffer) {
        int index = 0;

        // 1. 验证魔术字
        byte magic = buffer.getByte(index++);
        Assert.isTrue(magic == MAGIC_NUMBER, "无效的协议魔术字: " + magic);

        // 2. 验证版本号
        byte version = buffer.getByte(index++);
        Assert.isTrue(version == PROTOCOL_VERSION, "不支持的协议版本: " + version);

        // 3. 读取消息类型
        byte messageType = buffer.getByte(index++);
        Assert.isTrue(isValidMessageType(messageType), "无效的消息类型: " + messageType);

        // 4. 读取消息标志（暂时跳过）
        byte messageFlags = buffer.getByte(index++);

        // 5. 读取消息长度
        int messageLength = buffer.getInt(index);
        index += 4;

        Assert.isTrue(messageLength == buffer.length(), "消息长度不匹配，期望: " + messageLength + ", 实际: " + buffer.length());

        return new ProtocolHeader(magic, version, messageType, messageFlags, messageLength, index);
    }

    /**
     * 解析消息内容
     */
    private IotDeviceMessage parseMessageContent(Buffer buffer, ProtocolHeader header) {
        int index = header.getNextIndex();

        // 1. 读取消息ID
        short messageIdLength = buffer.getShort(index);
        index += 2;
        String messageId = buffer.getString(index, index + messageIdLength, StandardCharsets.UTF_8.name());
        index += messageIdLength;

        // 2. 读取方法名
        short methodLength = buffer.getShort(index);
        index += 2;
        String method = buffer.getString(index, index + methodLength, StandardCharsets.UTF_8.name());
        index += methodLength;

        // 3. 解析消息体
        return parseMessageBody(buffer, index, header.getMessageType(), messageId, method);
    }

    /**
     * 解析消息体
     */
    private IotDeviceMessage parseMessageBody(Buffer buffer, int startIndex, byte messageType,
                                              String messageId, String method) {
        if (startIndex >= buffer.length()) {
            // 空消息体
            return IotDeviceMessage.of(messageId, method, null, null, null, null);
        }

        if (messageType == MessageType.RESPONSE) {
            // 响应消息：解析 code + msg + data
            return parseResponseMessage(buffer, startIndex, messageId, method);
        } else {
            // 请求消息：解析 payload（可能是 params 或 data）
            Object payload = parseJsonData(buffer, startIndex, buffer.length());
            return IotDeviceMessage.of(messageId, method, payload, null, null, null);
        }
    }

    /**
     * 解析响应消息
     */
    private IotDeviceMessage parseResponseMessage(Buffer buffer, int startIndex, String messageId, String method) {
        int index = startIndex;

        // 1. 读取响应码
        Integer code = buffer.getInt(index);
        index += 4;

        // 2. 读取响应消息
        short msgLength = buffer.getShort(index);
        index += 2;
        String msg = msgLength > 0 ? buffer.getString(index, index + msgLength, StandardCharsets.UTF_8.name()) : null;
        index += msgLength;

        // 3. 读取响应数据
        Object data = null;
        if (index < buffer.length()) {
            data = parseJsonData(buffer, index, buffer.length());
        }

        return IotDeviceMessage.of(messageId, method, null, data, code, msg);
    }

    /**
     * 解析JSON数据
     */
    private Object parseJsonData(Buffer buffer, int startIndex, int endIndex) {
        if (startIndex >= endIndex) {
            return null;
        }

        try {
            String jsonStr = buffer.getString(startIndex, endIndex, StandardCharsets.UTF_8.name());
            if (StrUtil.isBlank(jsonStr)) {
                return null;
            }
            return JsonUtils.parseObject(jsonStr, Object.class);
        } catch (Exception e) {
            log.warn("[parseJsonData][JSON 解析失败，返回原始字符串]", e);
            return buffer.getString(startIndex, endIndex, StandardCharsets.UTF_8.name());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 验证消息类型是否有效
     */
    private boolean isValidMessageType(byte messageType) {
        return messageType == MessageType.REQUEST || messageType == MessageType.RESPONSE;
    }

    // ==================== 内部类 ====================

    /**
     * 协议头部信息
     */
    @Data
    @AllArgsConstructor
    private static class ProtocolHeader {
        private byte magic;
        private byte version;
        private byte messageType;
        private byte messageFlags;
        private int messageLength;
        private int nextIndex; // 指向消息内容开始位置
    }
}