package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * IoT TCP 帧编解码器接口
 * <p>
 * 用于解决 TCP 粘包/拆包问题，提供解码（拆包）和编码（加帧）能力
 *
 * @author 芋道源码
 */
public interface IotTcpFrameCodec {

    /**
     * 获取编解码器类型
     *
     * @return 编解码器类型
     */
    IotTcpCodecTypeEnum getType();

    /**
     * 创建解码器（RecordParser）
     * <p>
     * 每个连接调用一次，返回的 parser 需绑定到 socket.handler()
     *
     * @param handler 消息处理器，当收到完整的消息帧后回调
     * @return RecordParser 实例
     */
    RecordParser createDecodeParser(Handler<Buffer> handler);

    /**
     * 编码消息（加帧）
     * <p>
     * 根据不同的编解码类型添加帧头/分隔符
     *
     * @param data 原始数据
     * @return 编码后的数据（带帧头/分隔符）
     */
    Buffer encode(byte[] data);

}
