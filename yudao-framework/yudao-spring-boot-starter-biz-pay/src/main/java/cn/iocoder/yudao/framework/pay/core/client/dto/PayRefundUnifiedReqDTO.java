package cn.iocoder.yudao.framework.pay.core.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 统一 退款 Request DTO
 *
 * @author jason
 */
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PayRefundUnifiedReqDTO {

    /**
     * 用户 IP
     */
    private String userIp;

    // TODO @jason：这个是否为非必传字段呀，只需要传递 payTradeNo 字段即可。尽可能精简
    /**
     * https://api.mch.weixin.qq.com/v3/refund/domestic/refunds 中的 transaction_id
     * https://opendocs.alipay.com/apis alipay.trade.refund 中的 trade_no
     * 渠道订单号
     */
    private String channelOrderNo;

    /**
     * https://api.mch.weixin.qq.com/v3/refund/domestic/refunds 中的 out_trade_no
     * https://opendocs.alipay.com/apis alipay.trade.refund 中的 out_trade_no
     * 支付交易号 {PayOrderExtensionDO no字段} 和 渠道订单号 不能同时为空
     */
    private String payTradeNo;

    /**
     * https://api.mch.weixin.qq.com/v3/refund/domestic/refunds 中的 out_refund_no
     * https://opendocs.alipay.com/apis alipay.trade.refund 中的 out_trade_no
     * 退款请求单号  同一退款请求单号多次请求只退一笔。
     * 使用 商户的退款单号。{PayRefundDO 字段 merchantRefundNo}
     */
    @NotEmpty(message = "退款请求单号")
    private String merchantRefundId;

    /**
     * 退款原因
     */
    @NotEmpty(message = "退款原因不能为空")
    private String reason;

    /**
     * 退款金额，单位：分
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "支付金额必须大于零")
    private Long amount;

    /**
     * 退款结果 notify 回调地址， 支付宝退款不需要回调地址， 微信需要
     */
    @URL(message = "支付结果的 notify 回调地址必须是 URL 格式")
    private String notifyUrl;

}
