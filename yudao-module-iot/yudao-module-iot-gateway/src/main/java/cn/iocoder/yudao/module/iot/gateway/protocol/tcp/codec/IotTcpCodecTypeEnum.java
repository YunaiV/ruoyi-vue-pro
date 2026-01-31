package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.delimiter.IotTcpDelimiterFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.length.IotTcpFixedLengthFrameCodec;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.length.IotTcpLengthFieldFrameCodec;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
     */
    FIXED_LENGTH("fixed_length", IotTcpFixedLengthFrameCodec.class),

    /**
     * 基于分隔符的拆包
     */
    DELIMITER("delimiter", IotTcpDelimiterFrameCodec.class),

    /**
     * 基于长度字段的拆包
     */
    LENGTH_FIELD("length_field", IotTcpLengthFieldFrameCodec.class),
    ;

    /**
     * 类型标识
     */
    private final String type;
    /**
     * 编解码器类
     */
    private final Class<? extends IotTcpFrameCodec> codecClass;

    /**
     * 根据类型获取枚举
     *
     * @param type 类型标识
     * @return 枚举值
     */
    public static IotTcpCodecTypeEnum of(String type) {
        return ArrayUtil.firstMatch(e -> e.getType().equalsIgnoreCase(type), values());
    }

}
