package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.IotTcpConfig;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.delimiter.IotTcpDelimiterFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.length.IotTcpFixedLengthFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.length.IotTcpLengthFieldFrameCodec;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

/**
 * IoT TCP 拆包类型枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum IotTcpCodecTypeEnum {

    /**
     * 基于固定长度的拆包
     * <p>
     * 消息格式：固定长度的消息体
     * 需要配置：fixedLength（固定长度）
     */
    FIXED_LENGTH("fixed_length", IotTcpFixedLengthFrameCodec::new),

    /**
     * 基于分隔符的拆包
     * <p>
     * 消息格式：消息内容 + 分隔符
     * 需要配置：delimiter（分隔符）
     */
    DELIMITER("delimiter", IotTcpDelimiterFrameCodec::new),

    /**
     * 基于长度字段的拆包
     * <p>
     * 消息格式：[长度字段][消息体]
     * 需要配置：lengthFieldOffset（长度字段偏移量）、lengthFieldLength（长度字段长度）
     */
    LENGTH_FIELD("length_field", IotTcpLengthFieldFrameCodec::new),
    ;

    /**
     * 类型标识
     */
    private final String type;

    /**
     * Codec 创建工厂
     */
    private final Function<IotTcpConfig.CodecConfig, IotTcpFrameCodec> codecFactory;

    /**
     * 根据类型获取枚举
     *
     * @param type 类型标识
     * @return 枚举值
     */
    public static IotTcpCodecTypeEnum of(String type) {
        return ArrayUtil.firstMatch(e -> e.getType().equalsIgnoreCase(type), values());
    }

    /**
     * 创建 Codec 实例
     *
     * @param config 拆包配置
     * @return Codec 实例
     */
    public IotTcpFrameCodec createCodec(IotTcpConfig.CodecConfig config) {
        return codecFactory.apply(config);
    }

}
