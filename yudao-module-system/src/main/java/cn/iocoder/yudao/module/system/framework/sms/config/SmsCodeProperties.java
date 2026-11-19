package cn.iocoder.yudao.module.system.framework.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Duration;

@ConfigurationProperties(prefix = "yudao.sms-code")
@Validated
@Data
public class SmsCodeProperties {

    /**
     * 过期时间
     */
    @NotNull(message = "过期时间不能为空")
    private Duration expireTimes;
    /**
     * 短信发送频率
     */
    @NotNull(message = "短信发送频率不能为空")
    private Duration sendFrequency;
    /**
     * 每日发送最大数量
     */
    @NotNull(message = "每日发送最大数量不能为空")
    private Integer sendMaximumQuantityPerDay;
    /**
     * 每个 IP 每日发送最大数量，跨手机号累计
     */
    @NotNull(message = "每个 IP 每日发送最大数量不能为空")
    private Integer sendMaximumQuantityPerIpPerDay = 20;
    /**
     * 每个 IP 每小时发送最大数量，跨手机号累计，按自然小时计算
     *
     * 默认为每日数量的一半，避免短时间内耗尽当日额度
     */
    @NotNull(message = "每个 IP 每小时发送最大数量不能为空")
    private Integer sendMaximumQuantityPerIpPerHour = 10;
    /**
     * 每个验证码最大校验错误次数，达到后验证码作废，需要重新获取
     */
    @NotNull(message = "验证码最大校验错误次数不能为空")
    private Integer validateMaximumQuantity = 5;
    /**
     * 验证码最小值
     */
    @NotNull(message = "验证码最小值不能为空")
    private Integer beginCode;
    /**
     * 验证码最大值
     */
    @NotNull(message = "验证码最大值不能为空")
    private Integer endCode;

}
