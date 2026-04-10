package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.length;

import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpCodecTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodec;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.lang.Assert;

import java.util.concurrent.atomic.AtomicReference;

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

    /**
     * 最大帧长度（64KB），防止 DoS 攻击
     */
    private static final int MAX_FRAME_LENGTH = 65536;

    private final int lengthFieldOffset;
    private final int lengthFieldLength;
    private final int lengthAdjustment;
    private final int initialBytesToStrip;

    /**
     * 头部长度 = 长度字段偏移量 + 长度字段长度
     */
    private final int headerLength;

    public IotTcpLengthFieldFrameCodec(IotTcpConfig.CodecConfig config) {
        Assert.notNull(config.getLengthFieldOffset(), "lengthFieldOffset 不能为空");
        Assert.notNull(config.getLengthFieldLength(), "lengthFieldLength 不能为空");
        Assert.notNull(config.getLengthAdjustment(), "lengthAdjustment 不能为空");
        Assert.notNull(config.getInitialBytesToStrip(), "initialBytesToStrip 不能为空");
        this.lengthFieldOffset = config.getLengthFieldOffset();
        this.lengthFieldLength = config.getLengthFieldLength();
        this.lengthAdjustment = config.getLengthAdjustment();
        this.initialBytesToStrip = config.getInitialBytesToStrip();
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
        parser.maxRecordSize(MAX_FRAME_LENGTH); // 设置最大记录大小，防止 DoS 攻击
        final AtomicReference<Integer> bodyLength = new AtomicReference<>(null); // 消息体长度，null 表示读取头部阶段
        final AtomicReference<Buffer> headerBuffer = new AtomicReference<>(null); // 头部消息

        // 处理读取到的数据
        parser.handler(buffer -> {
            if (bodyLength.get() == null) {
                // 阶段 1: 读取头部，解析长度字段
                headerBuffer.set(buffer.copy());
                int length = readLength(buffer, lengthFieldOffset, lengthFieldLength);
                int frameBodyLength = length + lengthAdjustment;
                // 检查帧长度是否合法
                if (frameBodyLength < 0) {
                    throw new IllegalStateException(String.format(
                            "[createDecodeParser][帧长度异常，length: %d, frameBodyLength: %d]",
                            length, frameBodyLength));
                }
                // 消息体为空，抛出异常
                if (frameBodyLength == 0) {
                    throw new IllegalStateException("[createDecodeParser][消息体不能为空]");
                }

                // 【重要】切换到读取消息体模式
                bodyLength.set(frameBodyLength);
                parser.fixedSizeMode(frameBodyLength);
            } else {
                // 阶段 2: 读取消息体，组装完整帧
                Buffer frame = processFrame(headerBuffer.get(), buffer);
                // 重置状态，准备读取下一帧
                bodyLength.set(null);
                headerBuffer.set(null);
                parser.fixedSizeMode(headerLength);

                // 【重要】处理完整消息
                handler.handle(frame);
            }
        });
        parser.exceptionHandler(ex -> {
            throw new RuntimeException("[createDecodeParser][解析异常]", ex);
        });
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
    @SuppressWarnings("EnhancedSwitchMigration")
    private int readLength(Buffer buffer, int offset, int length) {
        switch (length) {
            case 1:
                return buffer.getUnsignedByte(offset);
            case 2:
                return buffer.getUnsignedShort(offset);
            case 4:
                return buffer.getInt(offset);
            default:
                throw new IllegalArgumentException("不支持的长度字段长度: " + length);
        }
    }

    /**
     * 向 Buffer 中写入长度字段
     */
    private void writeLength(Buffer buffer, int length, int fieldLength) {
        switch (fieldLength) {
            case 1:
                buffer.appendByte((byte) length);
                break;
            case 2:
                buffer.appendShort((short) length);
                break;
            case 4:
                buffer.appendInt(length);
                break;
            default:
                throw new IllegalArgumentException("不支持的长度字段长度: " + fieldLength);
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
