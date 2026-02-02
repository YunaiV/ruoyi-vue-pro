package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.delimiter;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpCodecTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodec;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.lang.Assert;

/**
 * IoT TCP 分隔符帧编解码器
 * <p>
 * 基于分隔符的拆包策略，消息格式：消息内容 + 分隔符
 * <p>
 * 支持的分隔符：
 * <ul>
 *   <li>\n - 换行符</li>
 *   <li>\r - 回车符</li>
 *   <li>\r\n - 回车换行</li>
 *   <li>自定义字符串</li>
 * </ul>
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpDelimiterFrameCodec implements IotTcpFrameCodec {

    /**
     * 最大记录大小（64KB），防止 DoS 攻击
     */
    private static final int MAX_RECORD_SIZE = 65536;

    /**
     * 解析后的分隔符字节数组
     */
    private final byte[] delimiterBytes;

    public IotTcpDelimiterFrameCodec(IotTcpConfig.CodecConfig config) {
        Assert.notBlank(config.getDelimiter(), "delimiter 不能为空");
        this.delimiterBytes = parseDelimiter(config.getDelimiter());
    }

    @Override
    public IotTcpCodecTypeEnum getType() {
        return IotTcpCodecTypeEnum.DELIMITER;
    }

    @Override
    public RecordParser createDecodeParser(Handler<Buffer> handler) {
        RecordParser parser = RecordParser.newDelimited(Buffer.buffer(delimiterBytes));
        parser.maxRecordSize(MAX_RECORD_SIZE); // 设置最大记录大小，防止 DoS 攻击
        // 处理完整消息（不包含分隔符）
        parser.handler(handler);
        parser.exceptionHandler(ex -> {
            throw new RuntimeException("[createDecodeParser][解析异常]", ex);
        });
        return parser;
    }

    @Override
    public Buffer encode(byte[] data) {
        Buffer buffer = Buffer.buffer();
        buffer.appendBytes(data);
        buffer.appendBytes(delimiterBytes);
        return buffer;
    }

    /**
     * 解析分隔符字符串为字节数组
     * <p>
     * 支持转义字符：\n、\r、\r\n、\t
     *
     * @param delimiter 分隔符字符串
     * @return 分隔符字节数组
     */
    private byte[] parseDelimiter(String delimiter) {
        // 处理转义字符
        String parsed = delimiter
                .replace("\\r\\n", "\r\n")
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\t", "\t");
        return StrUtil.utf8Bytes(parsed);
    }

}
