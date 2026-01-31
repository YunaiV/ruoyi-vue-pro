package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.delimiter;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpCodecTypeEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpFrameCodec;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

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

    private final IotTcpConfig.CodecConfig config;

    /**
     * 解析后的分隔符字节数组
     */
    private final byte[] delimiterBytes;

    /**
     * 最大帧长度
     */
    // TODO @AI：最大帧数要不去掉；简洁一点；包括其他地方的配置项；
    private final int maxFrameLength;

    public IotTcpDelimiterFrameCodec(IotTcpConfig.CodecConfig config) {
        this.config = config;
        // TODO @AI：禁止为空；
        this.delimiterBytes = parseDelimiter(config.getDelimiter());
        this.maxFrameLength = config.getMaxFrameLength() != null ? config.getMaxFrameLength() : 1048576;
    }

    @Override
    public IotTcpCodecTypeEnum getType() {
        return IotTcpCodecTypeEnum.DELIMITER;
    }

    @Override
    public RecordParser createDecodeParser(Handler<Buffer> handler) {
        RecordParser parser = RecordParser.newDelimited(Buffer.buffer(delimiterBytes));

        parser.handler(buffer -> {
            // 检查帧长度是否超过限制
            if (buffer.length() > maxFrameLength) {
                log.warn("[createDecodeParser][帧长度超过限制，length: {}, maxFrameLength: {}]",
                        buffer.length(), maxFrameLength);
                return;
            }
            // 处理完整消息（不包含分隔符）
            handler.handle(buffer);
        });

        // TODO @AI：异常处理；
        parser.exceptionHandler(ex -> log.error("[createDecodeParser][解析异常]", ex));
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
        if (StrUtil.isBlank(delimiter)) {
            // 默认使用换行符
            return new byte[]{'\n'};
        }

        // 处理转义字符
        // TODO @AI：是否必要？不调整感觉也没问题？用户自己写对就 ok 了是哇？
        String parsed = delimiter
                .replace("\\r\\n", "\r\n")
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\t", "\t");
        return parsed.getBytes();
    }

}
