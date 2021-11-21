package cn.iocoder.yudao.framework.pay.core.client.dto;

import cn.iocoder.yudao.framework.pay.core.enums.PayChannelRespEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
/**
 * 统一 退款 Response DTO
 *
 * @author jason
 */
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PayRefundUnifiedRespDTO {



    /**
     * 渠道的通用返回结果
     */
    private PayChannelRespEnum respEnum;



    /**
     * 渠道退款单号
     */
    private String channelRefundNo;


    /**
     * https://api.mch.weixin.qq.com/v3/refund/domestic/refunds 中的 out_trade_no
     * https://opendocs.alipay.com/apis alipay.trade.refund 中的 out_trade_no
     * 支付交易号 {PayOrderExtensionDO no字段} 和 渠道订单号 不能同时为空
     */
    private String payTradeNo;


    /**
     * https://api.mch.weixin.qq.com/v3/refund/domestic/refunds 中的 out_refund_no
     * https://opendocs.alipay.com/apis alipay.trade.refund 中的 out_request_no
     * 退款请求单号  同一退款请求单号多次请求只退一笔。
     */
    private String refundReqNo;



    /**
     * 调用异常错误信息
     */
    private String exceptionMsg;


    /**
     * 渠道的错误码
     */
    private String channelErrCode;


    /**
     * 渠道的错误描述
     */
    private String channelErrMsg;

    //TODO 退款资金渠 ？？？
}
