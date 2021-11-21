package cn.iocoder.yudao.coreservice.modules.pay.service.order.bo;

import cn.iocoder.yudao.coreservice.modules.pay.enums.order.PayRefundTypeEnum;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelRespEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefundPostReqBO {


    /**
     * 渠道的通用返回结果
     */
    private PayChannelRespEnum respEnum;



    private PayRefundTypeEnum refundTypeEnum;

    /**
     * 已退款的总金额
     */
    private Long  refundedAmount;

    /**
     * 本次退款金额
     */
    private Long refundAmount;

   /**
     * 已退款次数
     */
    private Integer refundedTimes;


    /**
     * 订单编号
     */
    private Long  orderId;

    /**
     * 退款单编号
     */
    private Long refundId;


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


}
