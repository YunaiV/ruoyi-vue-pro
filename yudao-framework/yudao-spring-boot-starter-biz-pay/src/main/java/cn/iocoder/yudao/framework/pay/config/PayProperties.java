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
     * 支付回调地址
     * 注意，支付渠道统一回调到 payNotifyUrl 地址，由支付模块统一处理；然后，自己的支付模块，在回调 PayAppDO.payNotifyUrl 地址
     */
    @NotEmpty(message = "支付回调地址不能为空")
    @URL(message = "支付回调地址的格式必须是 URL")
    private String payNotifyUrl;
    /**
     * 退款回调地址
     * 注意点，同 {@link #payNotifyUrl} 属性
     */
    @NotEmpty(message = "退款回调地址不能为空")
    @URL(message = "退款回调地址的格式必须是 URL")
    private String refundNotifyUrl;


    /**
     * 支付完成的返回地址
     */
    @URL(message = "支付返回的地址的格式必须是 URL")
    @NotEmpty(message = "支付返回的地址不能为空")
    private String payReturnUrl;

}
