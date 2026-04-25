package cn.iocoder.yudao.module.trade.service.wholesale.pay.bo;

import lombok.Data;

/**
 * 批发退款结果 BO
 *
 * @author deepay
 */
@Data
public class WholesaleRefundResultBO {

    /** 退款单编号（来自 PayRefundApi） */
    private Long refundId;

    /** 商户订单号 */
    private String merchantOrderId;

    /** 商户退款单号 */
    private String merchantRefundId;

    /** 退款金额（分） */
    private Integer refundPrice;

    /** 退款状态：WAITING / SUCCESS / FAILURE */
    private String status;

    /** 退款原因 */
    private String reason;

}
