package cn.iocoder.dashboard.framework.captcha.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Duration;

@ConfigurationProperties(prefix = "yudao.captcha")
@Validated
@Data
public class CaptchaProperties {

    /**
     * 验证码的过期时间
     */
    @NotNull(message = "验证码的过期时间不为空")
    private Duration timeout;
    /**
     * 验证码的高度
     */
    @NotNull(message = "验证码的高度不能为空")
    private Integer height;
    /**
     * 验证码的宽度
     */
    @NotNull(message = "验证码的宽度不能为空")
    private Integer width;

}
