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
     *
     * 实际上，对应的 PayNotifyController 的 notifyOrder 方法的 URL
     *
     * 回调顺序：支付渠道（支付宝支付、微信支付） => yudao-module-pay 的 orderNotifyUrl 地址 => 业务的 PayAppDO.orderNotifyUrl 地址
     */
    @NotEmpty(message = "支付回调地址不能为空")
    @URL(message = "支付回调地址的格式必须是 URL")
    private String orderNotifyUrl;

    /**
     * 退款回调地址
     *
     * 实际上，对应的 PayNotifyController 的 notifyRefund 方法的 URL
     *
     * 回调顺序：支付渠道（支付宝支付、微信支付） => yudao-module-pay 的 refundNotifyUrl 地址 => 业务的 PayAppDO.notifyRefundUrl 地址
     */
    @NotEmpty(message = "支付回调地址不能为空")
    @URL(message = "支付回调地址的格式必须是 URL")
    private String refundNotifyUrl;

}
