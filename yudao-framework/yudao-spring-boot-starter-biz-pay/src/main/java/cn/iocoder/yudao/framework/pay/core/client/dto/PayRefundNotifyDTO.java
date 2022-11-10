package cn.iocoder.yudao.framework.pay.core.client.dto;

import cn.iocoder.yudao.framework.pay.core.enums.PayNotifyRefundStatusEnum;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 从渠道返回数据中解析得到的支付退款通知的Notify DTO
 *
 * @author jason
 */
@Data
@ToString
@Builder
public class PayRefundNotifyDTO {

    /**
     * 支付渠道编号
     */
    private String channelOrderNo;


    /**
     * 交易订单号，根据规则生成
     * 调用支付渠道时，使用该字段作为对接的订单号。
     * 1. 调用微信支付 https://api.mch.weixin.qq.com/pay/unifiedorder 时，使用该字段作为 out_trade_no
     * 2. 调用支付宝 https://opendocs.alipay.com/apis 时，使用该字段作为 out_trade_no
     *  这里对应 pay_extension 里面的 no
     * 例如说，P202110132239124200055
     */
    private String tradeNo;

    /**
     * https://api.mch.weixin.qq.com/v3/refund/domestic/refunds 中的 out_refund_no
     * https://opendocs.alipay.com/apis alipay.trade.refund 中的 out_request_no
     * 退款请求号。
     * 标识一次退款请求，需要保证在交易号下唯一，如需部分退款，则此参数必传。
     * 注：针对同一次退款请求，如果调用接口失败或异常了，重试时需要保证退款请求号不能变更，
     * 防止该笔交易重复退款。支付宝会保证同样的退款请求号多次请求只会退一次。
     * 退款单请求号，根据规则生成
     *
     * 例如说，RR202109181134287570000
     */
    private String reqNo;


    /**
     * 退款是否成功
     */
    private PayNotifyRefundStatusEnum status;



    /**
     * 退款成功时间
     */
    private LocalDateTime refundSuccessTime;


}
