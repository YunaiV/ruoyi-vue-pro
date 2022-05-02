package cn.iocoder.yudao.framework.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@ConfigurationProperties(prefix = "yudao.security")
@Validated
@Data
public class SecurityProperties {

    /**
     * HTTP 请求时，访问令牌的请求 Header
     */
    @NotEmpty(message = "Token Header 不能为空")
    private String tokenHeader;
    /**
     * Token 过期时间
     */
    @NotNull(message = "Token 过期时间不能为空")
    private Duration tokenTimeout;
    /**
     * Session 过期时间
     *
     * 当 User 用户超过当前时间未操作，则 Session 会过期
     */
    @NotNull(message = "Session 过期时间不能为空")
    private Duration sessionTimeout;

    /**
     * mock 模式的开关
     */
    @NotNull(message = "mock 模式的开关不能为空")
    private Boolean mockEnable;
    /**
     * mock 模式的密钥
     * 一定要配置密钥，保证安全性
     */
    @NotEmpty(message = "mock 模式的密钥不能为空") // 这里设置了一个默认值，因为实际上只有 mockEnable 为 true 时才需要配置。
    private String mockSecret = "yudaoyuanma";

}
