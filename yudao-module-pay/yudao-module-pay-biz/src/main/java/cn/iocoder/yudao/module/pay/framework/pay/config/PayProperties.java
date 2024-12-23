package cn.iocoder.yudao.module.pay.framework.pay.config;

import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix = "yudao.pay")
@Validated
@Data
public class PayProperties {

    private static final String ORDER_NO_PREFIX = "P";
    private static final String REFUND_NO_PREFIX = "R";

    private static final String WALLET_PAY_APP_KEY_DEFAULT = "wallet";

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

    /**
     * 转账回调地址
     *
     * 实际上，对应的 PayNotifyController 的 notifyTransfer 方法的 URL
     *
     * 回调顺序：支付渠道（支付宝支付、微信支付） => yudao-module-pay 的 transferNotifyUrl 地址 => 业务的 PayAppDO.transferNotifyUrl 地址
     */
    private String transferNotifyUrl;

    /**
     * 支付订单 no 的前缀
     */
    @NotEmpty(message = "支付订单 no 的前缀不能为空")
    private String orderNoPrefix = ORDER_NO_PREFIX;

    /**
     * 退款订单 no 的前缀
     */
    @NotEmpty(message = "退款订单 no 的前缀不能为空")
    private String refundNoPrefix = REFUND_NO_PREFIX;

    /**
     * 钱包支付应用 AppKey
     */
    @NotEmpty(message = "钱包支付应用 AppKey 不能为空")
    private String walletPayAppKey = WALLET_PAY_APP_KEY_DEFAULT;

}
