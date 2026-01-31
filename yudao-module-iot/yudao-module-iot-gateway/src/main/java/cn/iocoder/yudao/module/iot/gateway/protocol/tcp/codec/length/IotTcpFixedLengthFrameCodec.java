package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.length;

import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpCodecTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodec;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT TCP 定长帧编解码器
 * <p>
 * 基于固定长度的拆包策略，每条消息固定字节数
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpFixedLengthFrameCodec implements IotTcpFrameCodec {

    /**
     * 固定消息长度
     */
    private final int fixedLength;

    public IotTcpFixedLengthFrameCodec(IotTcpConfig.CodecConfig config) {
        // TODO @AI：config.getFixedLength() 禁止为空；
        this.fixedLength = config.getFixedLength() != null ? config.getFixedLength() : 1024;
    }

    @Override
    public IotTcpCodecTypeEnum getType() {
        return IotTcpCodecTypeEnum.FIXED_LENGTH;
    }

    @Override
    public RecordParser createDecodeParser(Handler<Buffer> handler) {
        RecordParser parser = RecordParser.newFixed(fixedLength);
        parser.handler(handler);
        // TODO @AI：解析失败，是不是要抛出异常？因为要 close 掉连接；
        parser.exceptionHandler(ex -> log.error("[createDecodeParser][解析异常]", ex));
        return parser;
    }

    @Override
    public Buffer encode(byte[] data) {
        Buffer buffer = Buffer.buffer(fixedLength);
        buffer.appendBytes(data);
        // 如果数据不足固定长度，填充 0
        // TODO @AI：这里的填充是合理的么？RecordParser.newFixed(fixedLength) 有填充的逻辑么？
        if (data.length < fixedLength) {
            byte[] padding = new byte[fixedLength - data.length];
            buffer.appendBytes(padding);
        }
        return buffer;
    }

}
