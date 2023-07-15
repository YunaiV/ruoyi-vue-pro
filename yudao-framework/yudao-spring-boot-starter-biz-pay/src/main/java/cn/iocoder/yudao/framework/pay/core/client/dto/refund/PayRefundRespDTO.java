package cn.iocoder.yudao.framework.pay.core.client.dto.refund;

import cn.iocoder.yudao.framework.pay.core.enums.refund.PayRefundStatusRespEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 渠道退款订单 Response DTO
 *
 * @author jason
 */
@Data
public class PayRefundRespDTO {

    /**
     * 退款状态
     *
     * 枚举 {@link PayRefundStatusRespEnum}
     */
    private Integer status;

    /**
     * 外部退款号
     *
     * 对应 PayRefundDO 的 no 字段
     */
    private String outRefundNo;

    /**
     * 渠道退款单号
     *
     * 对应 PayRefundDO.channelRefundNo 字段
     */
    private String channelRefundNo;

    /**
     * 退款成功时间
     */
    private LocalDateTime successTime;

    /**
     * 原始的异步通知结果
     */
    private Object rawData;

}
