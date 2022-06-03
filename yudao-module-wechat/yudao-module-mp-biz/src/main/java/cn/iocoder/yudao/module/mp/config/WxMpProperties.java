package cn.iocoder.yudao.module.mp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * wechat mp properties
 */
@Data
@ConfigurationProperties(prefix = "wx.mp")
public class WxMpProperties {
    /**
     * 是否使用redis存储access token
     */
    private boolean useRedis;

    private String defaultContent;

    /**
     * redis 配置
     */
    private RedisConfig redisConfig;

    @Data
    public static class RedisConfig {
        /**
         * redis服务器 主机地址
         */
        private String host;

        /**
         * redis服务器 端口号
         */
        private Integer port;

        /**
         * redis服务器 密码
         */
        private String password;

        /**
         * redis 服务连接超时时间
         */
        private Integer timeout;
    }

}
