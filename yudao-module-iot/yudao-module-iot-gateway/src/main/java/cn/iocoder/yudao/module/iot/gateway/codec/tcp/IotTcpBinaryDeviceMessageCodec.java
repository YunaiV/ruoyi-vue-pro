package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.gateway.codec.IotDeviceMessageCodec;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * TCP 二进制格式 {@link IotDeviceMessage} 编解码器
 * <p>
 * 二进制协议格式（所有数值使用大端序）：
 *
 * <pre>
 * +--------+--------+--------+---------------------------+--------+--------+
 * | 魔术字 | 版本号 | 消息类型|         消息长度(4 字节)          |
 * +--------+--------+--------+---------------------------+--------+--------+
 * |           消息 ID 长度(2 字节)        |      消息 ID (变长字符串)         |
 * +--------+--------+--------+--------+--------+--------+--------+--------+
 * |           方法名长度(2 字节)        |      方法名(变长字符串)         |
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

    /**
     * 响应消息类型
     */
    private static final byte RESPONSE = (byte) 0x02;

    /**
     * 协议头部固定长度（魔术字 + 版本号 + 消息类型 + 消息长度）
     */
    private static final int HEADER_FIXED_LENGTH = 7;

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
            // 3. 构建完整消息
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
            // 解析协议头部和消息内容
            int index = 0;
            // 1. 验证魔术字
            byte magic = buffer.getByte(index++);
            Assert.isTrue(magic == MAGIC_NUMBER, "无效的协议魔术字: " + magic);

            // 2. 验证版本号
            byte version = buffer.getByte(index++);
            Assert.isTrue(version == PROTOCOL_VERSION, "不支持的协议版本: " + version);

            // 3. 读取消息类型
            byte messageType = buffer.getByte(index++);
            // 直接验证消息类型，无需抽取方法
            Assert.isTrue(messageType == REQUEST || messageType == RESPONSE,
                    "无效的消息类型: " + messageType);

            // 4. 读取消息长度
            int messageLength = buffer.getInt(index);
            index += 4;
            Assert.isTrue(messageLength == buffer.length(),
                    "消息长度不匹配，期望: " + messageLength + ", 实际: " + buffer.length());

            // 5. 读取消息 ID
            short messageIdLength = buffer.getShort(index);
            index += 2;
            String messageId = buffer.getString(index, index + messageIdLength, StandardCharsets.UTF_8.name());
            index += messageIdLength;

            // 6. 读取方法名
            short methodLength = buffer.getShort(index);
            index += 2;
            String method = buffer.getString(index, index + methodLength, StandardCharsets.UTF_8.name());
            index += methodLength;

            // 7. 解析消息体
            return parseMessageBody(buffer, index, messageType, messageId, method);
        } catch (Exception e) {
            log.error("[decode][TCP 二进制消息解码失败，数据长度: {}]", bytes.length, e);
            throw new RuntimeException("TCP 二进制消息解码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 确定消息类型
     * 优化后的判断逻辑：有响应字段就是响应消息，否则就是请求消息
     */
    private byte determineMessageType(IotDeviceMessage message) {
        // 判断是否为响应消息：有响应码或响应消息时为响应
        if (message.getCode() != null) {
            return RESPONSE;
        }
        // 默认为请求消息
        return REQUEST;
    }

    /**
     * 构建消息体
     */
    private byte[] buildMessageBody(IotDeviceMessage message, byte messageType) {
        Buffer bodyBuffer = Buffer.buffer();
        if (messageType == RESPONSE) {
            // code
            bodyBuffer.appendInt(message.getCode() != null ? message.getCode() : 0);
            // msg
            String msg = message.getMsg() != null ? message.getMsg() : "";
            byte[] msgBytes = StrUtil.utf8Bytes(msg);
            bodyBuffer.appendShort((short) msgBytes.length);
            bodyBuffer.appendBytes(msgBytes);
            // data
            if (message.getData() != null) {
                bodyBuffer.appendBytes(JsonUtils.toJsonByte(message.getData()));
            }
        } else {
            // 请求消息只处理 params 参数
            // TODO @haohao：如果为空，是不是得写个长度 0 哈？
            if (message.getParams() != null) {
                bodyBuffer.appendBytes(JsonUtils.toJsonByte(message.getParams()));
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
        // 2. 预留消息长度位置（在 5. 更新消息长度）
        int lengthPosition = buffer.length();
        buffer.appendInt(0);
        // 3. 写入消息 ID
        String messageId = StrUtil.isNotBlank(message.getRequestId()) ? message.getRequestId()
                : IotDeviceMessageUtils.generateMessageId();
        byte[] messageIdBytes = StrUtil.utf8Bytes(messageId);
        buffer.appendShort((short) messageIdBytes.length);
        buffer.appendBytes(messageIdBytes);
        // 4. 写入方法名
        byte[] methodBytes = StrUtil.utf8Bytes(message.getMethod());
        buffer.appendShort((short) methodBytes.length);
        buffer.appendBytes(methodBytes);
        // 5. 写入消息体
        buffer.appendBytes(bodyData);
        // 6. 更新消息长度
        buffer.setInt(lengthPosition, buffer.length());
        return buffer.getBytes();
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

        if (messageType == RESPONSE) {
            // 响应消息：解析 code + msg + data
            return parseResponseMessage(buffer, startIndex, messageId, method);
        } else {
            // 请求消息：解析 payload
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
     * 解析 JSON 数据
     */
    private Object parseJsonData(Buffer buffer, int startIndex, int endIndex) {
        if (startIndex >= endIndex) {
            return null;
        }
        try {
            String jsonStr = buffer.getString(startIndex, endIndex, StandardCharsets.UTF_8.name());
            return JsonUtils.parseObject(jsonStr, Object.class);
        } catch (Exception e) {
            log.warn("[parseJsonData][JSON 解析失败，返回原始字符串]", e);
            return buffer.getString(startIndex, endIndex, StandardCharsets.UTF_8.name());
        }
    }

    /**
     * 快速检测是否为二进制格式
     *
     * @param data 数据
     * @return 是否为二进制格式
     */
    public static boolean isBinaryFormatQuick(byte[] data) {
        return data != null && data.length >= 1 && data[0] == MAGIC_NUMBER;
    }

}