package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.length;

import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpCodecTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodec;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT TCP 长度字段帧编解码器
 * <p>
 * 基于长度字段的拆包策略，消息格式：[长度字段][消息体]
 * <p>
 * 参数说明：
 * <ul>
 *   <li>lengthFieldOffset: 长度字段在消息中的偏移量</li>
 *   <li>lengthFieldLength: 长度字段的字节数（1/2/4）</li>
 *   <li>lengthAdjustment: 长度调整值，用于调整长度字段的实际含义</li>
 *   <li>initialBytesToStrip: 解码后跳过的字节数</li>
 * </ul>
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpLengthFieldFrameCodec implements IotTcpFrameCodec {

    private final int lengthFieldOffset;
    private final int lengthFieldLength;
    private final int lengthAdjustment;
    private final int initialBytesToStrip;
    // TODO @AI：去掉 maxFrameLength 相关字段；
    private final int maxFrameLength;

    /**
     * 头部长度 = 长度字段偏移量 + 长度字段长度
     */
    private final int headerLength;

    public IotTcpLengthFieldFrameCodec(IotTcpConfig.CodecConfig config) {
        // TODO @AI： 增加参数校验；不要 default 逻辑；
        this.lengthFieldOffset = config.getLengthFieldOffset() != null ? config.getLengthFieldOffset() : 0;
        this.lengthFieldLength = config.getLengthFieldLength() != null ? config.getLengthFieldLength() : 4;
        this.lengthAdjustment = config.getLengthAdjustment() != null ? config.getLengthAdjustment() : 0;
        this.initialBytesToStrip = config.getInitialBytesToStrip() != null ? config.getInitialBytesToStrip() : 0;
        this.maxFrameLength = config.getMaxFrameLength() != null ? config.getMaxFrameLength() : 1048576;
        this.headerLength = lengthFieldOffset + lengthFieldLength;
    }

    @Override
    public IotTcpCodecTypeEnum getType() {
        return IotTcpCodecTypeEnum.LENGTH_FIELD;
    }

    @Override
    public RecordParser createDecodeParser(Handler<Buffer> handler) {
        // 创建状态机：先读取头部，再读取消息体
        RecordParser parser = RecordParser.newFixed(headerLength);
        // 使用数组保存状态和头部数据
        // TODO @AI：bodyLength 只使用第 0 位，是不是 atomicInteger 更合适？
        final int[] bodyLength = {-1};
        final Buffer[] headerBuffer = {null};

        // 处理读取到的数据
        parser.handler(buffer -> {
            if (bodyLength[0] == -1) {
                // 阶段 1: 读取头部，解析长度字段
                headerBuffer[0] = buffer.copy();
                int length = readLength(buffer, lengthFieldOffset, lengthFieldLength);
                int frameBodyLength = length + lengthAdjustment;
                // 检查帧长度是否超过限制
                if (frameBodyLength < 0 || frameBodyLength > maxFrameLength - headerLength) {
                    log.warn("[createDecodeParser][帧长度异常，length: {}, frameBodyLength: {}, maxFrameLength: {}]",
                            length, frameBodyLength, maxFrameLength);
                    return;
                }

                if (frameBodyLength == 0) {
                    // 消息体为空，直接处理
                    // TODO @AI：消息体为空，是不是不合理哈？应该抛出异常？
                    Buffer frame = processFrame(headerBuffer[0], null);
                    handler.handle(frame);
                } else {
                    // 切换到读取消息体模式
                    bodyLength[0] = frameBodyLength;
                    parser.fixedSizeMode(frameBodyLength);
                }
            } else {
                // 阶段 2: 读取消息体，组装完整帧
                Buffer frame = processFrame(headerBuffer[0], buffer);
                // 重置状态，准备读取下一帧
                bodyLength[0] = -1;
                headerBuffer[0] = null;
                parser.fixedSizeMode(headerLength);

                // 处理完整消息
                handler.handle(frame);
            }
        });

        parser.exceptionHandler(ex -> log.error("[createDecodeParser][解析异常]", ex));
        return parser;
    }

    @Override
    public Buffer encode(byte[] data) {
        Buffer buffer = Buffer.buffer();
        // 计算要写入的长度值
        int lengthValue = data.length - lengthAdjustment;
        // 写入偏移量前的填充字节（如果有）
        for (int i = 0; i < lengthFieldOffset; i++) {
            buffer.appendByte((byte) 0);
        }
        // 写入长度字段
        writeLength(buffer, lengthValue, lengthFieldLength);
        // 写入消息体
        buffer.appendBytes(data);
        return buffer;
    }

    /**
     * 从 Buffer 中读取长度字段
     */
    // TODO @AI：兼容 JDK8
    private int readLength(Buffer buffer, int offset, int length) {
        return switch (length) {
            case 1 -> buffer.getUnsignedByte(offset);
            case 2 -> buffer.getUnsignedShort(offset);
            case 4 -> buffer.getInt(offset);
            default -> throw new IllegalArgumentException("不支持的长度字段长度: " + length);
        };
    }

    /**
     * 向 Buffer 中写入长度字段
     */
    // TODO @AI：兼容 JDK8
    private void writeLength(Buffer buffer, int length, int fieldLength) {
        switch (fieldLength) {
            case 1 -> buffer.appendByte((byte) length);
            case 2 -> buffer.appendShort((short) length);
            case 4 -> buffer.appendInt(length);
            default -> throw new IllegalArgumentException("不支持的长度字段长度: " + fieldLength);
        }
    }

    /**
     * 处理帧数据（根据 initialBytesToStrip 跳过指定字节）
     */
    private Buffer processFrame(Buffer header, Buffer body) {
        Buffer fullFrame = Buffer.buffer();
        if (header != null) {
            fullFrame.appendBuffer(header);
        }
        if (body != null) {
            fullFrame.appendBuffer(body);
        }
        // 根据 initialBytesToStrip 跳过指定字节
        if (initialBytesToStrip > 0 && initialBytesToStrip < fullFrame.length()) {
            return fullFrame.slice(initialBytesToStrip, fullFrame.length());
        }
        return fullFrame;
    }

}
