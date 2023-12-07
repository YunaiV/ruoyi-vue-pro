package cn.iocoder.yudao.framework.sms.core.property;

import cn.iocoder.yudao.framework.sms.core.enums.SmsChannelEnum;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 短信渠道配置类
 *
 * @author zzf
 * @since 2021/1/25 17:01
 */
@Data
@Validated
public class SmsChannelProperties {

    /**
     * 渠道编号
     */
    @NotNull(message = "短信渠道 ID 不能为空")
    private Long id;
    /**
     * 短信签名
     */
    @NotEmpty(message = "短信签名不能为空")
    private String signature;
    /**
     * 渠道编码
     *
     * 枚举 {@link SmsChannelEnum}
     */
    @NotEmpty(message = "渠道编码不能为空")
    private String code;
    /**
     * 短信 API 的账号
     */
    @NotEmpty(message = "短信 API 的账号不能为空")
    private String apiKey;
    /**
     * 短信 API 的密钥
     */
    @NotEmpty(message = "短信 API 的密钥不能为空")
    private String apiSecret;
    /**
     * 短信发送回调 URL
     */
    private String callbackUrl;

}
