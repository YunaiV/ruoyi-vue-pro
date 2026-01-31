package cn.iocoder.yudao.module.iot.gateway.protocol.tcp;

import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.codec.IotTcpCodecTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * IoT TCP 协议配置
 *
 * @author 芋道源码
 */
@Data
public class IotTcpConfig {

    /**
     * 最大连接数
     */
    @NotNull(message = "最大连接数不能为空")
    @Min(value = 1, message = "最大连接数必须大于 0")
    private Integer maxConnections = 1000;
    /**
     * 心跳超时时间（毫秒）
     */
    @NotNull(message = "心跳超时时间不能为空")
    @Min(value = 1000, message = "心跳超时时间必须大于 1000 毫秒")
    private Long keepAliveTimeoutMs = 30000L;

    /**
     * 是否启用 SSL
     */
    @NotNull(message = "是否启用 SSL 不能为空")
    private Boolean sslEnabled = false;
    /**
     * SSL 证书路径
     */
    private String sslCertPath;
    /**
     * SSL 私钥路径
     */
    private String sslKeyPath;

    /**
     * 拆包配置
     */
    @Valid
    private CodecConfig codec;

    /**
     * TCP 拆包配置
     */
    @Data
    public static class CodecConfig {

        /**
         * 拆包类型
         *
         * @see IotTcpCodecTypeEnum
         */
        @NotNull(message = "拆包类型不能为空")
        private String type;

        /**
         * LENGTH_FIELD: 长度字段偏移量
         * <p>
         * 表示长度字段在消息中的起始位置（从0开始）
         */
        private Integer lengthFieldOffset;
        /**
         * LENGTH_FIELD: 长度字段长度（字节数）
         * <p>
         * 常见值：1（最大255）、2（最大65535）、4（最大2GB）
         */
        private Integer lengthFieldLength;
        /**
         * LENGTH_FIELD: 长度调整值
         * <p>
         * 用于调整长度字段的值，例如长度字段包含头部长度时需要减去头部长度
         */
        private Integer lengthAdjustment = 0;
        /**
         * LENGTH_FIELD: 跳过的初始字节数
         * <p>
         * 解码后跳过的字节数，通常等于 lengthFieldOffset + lengthFieldLength
         */
        private Integer initialBytesToStrip = 0;

        /**
         * DELIMITER: 分隔符
         * <p>
         * 支持转义字符：\n（换行）、\r（回车）、\r\n（回车换行）
         */
        private String delimiter;

        /**
         * FIXED_LENGTH: 固定消息长度（字节）
         * <p>
         * 每条消息的固定长度
         */
        private Integer fixedLength;

        /**
         * 最大帧长度（字节）
         * <p>
         * 防止内存溢出，默认 1MB
         */
        @NotNull(message = "最大帧长度不能为空")
        @Min(value = 1, message = "最大帧长度必须大于 0")
        private Integer maxFrameLength = 1048576;

    }

}
