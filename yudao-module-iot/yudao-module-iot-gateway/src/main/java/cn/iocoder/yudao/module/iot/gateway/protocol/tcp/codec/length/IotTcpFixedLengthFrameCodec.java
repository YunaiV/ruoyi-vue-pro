package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.length;

import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpCodecTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodec;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.lang.Assert;

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
        Assert.notNull(config.getFixedLength(), "fixedLength 不能为空");
        this.fixedLength = config.getFixedLength();
    }

    @Override
    public IotTcpCodecTypeEnum getType() {
        return IotTcpCodecTypeEnum.FIXED_LENGTH;
    }

    @Override
    public RecordParser createDecodeParser(Handler<Buffer> handler) {
        RecordParser parser = RecordParser.newFixed(fixedLength);
        parser.handler(handler);
        parser.exceptionHandler(ex -> {
            throw new RuntimeException("[createDecodeParser][解析异常]", ex);
        });
        return parser;
    }

    @Override
    public Buffer encode(byte[] data) {
        // 校验数据长度不能超过固定长度
        if (data.length > fixedLength) {
            throw new IllegalArgumentException(String.format(
                    "数据长度 %d 超过固定长度 %d", data.length, fixedLength));
        }
        Buffer buffer = Buffer.buffer(fixedLength);
        buffer.appendBytes(data);
        // 如果数据不足固定长度，填充 0（RecordParser.newFixed 解码时按固定长度读取，所以发送端需要填充）
        if (data.length < fixedLength) {
            byte[] padding = new byte[fixedLength - data.length];
            buffer.appendBytes(padding);
        }
        return buffer;
    }

}
