package cn.iocoder.yudao.framework.pay.config;

import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix = "yudao.pay")
@Validated
@Data
public class PayProperties {

    /**
     * 回调地址
     *
     * 实际上，对应的 PayNotifyController 的 notifyCallback 方法的 URL
     *
     * 注意，支付渠道统一回调到 payNotifyUrl 地址，由支付模块统一处理；然后，自己的支付模块，在回调 PayAppDO.payNotifyUrl 地址
     */
    @NotEmpty(message = "回调地址不能为空")
    @URL(message = "回调地址的格式必须是 URL")
    private String callbackUrl;

    /**
     * 回跳地址
     *
     * 实际上，对应的 PayNotifyController 的 returnCallback 方法的 URL
     */
    @URL(message = "回跳地址的格式必须是 URL")
    @NotEmpty(message = "回跳地址不能为空")
    private String returnUrl;

}
