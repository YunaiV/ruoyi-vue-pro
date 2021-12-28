package cn.iocoder.yudao.coreservice.modules.pay.service.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 退款申请单 Response DTO
 */
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefundRespDTO {

    /**
     * 渠道返回结果
     * 退款处理中和退款成功  返回  1
     * 失败和其他情况 返回 2
     */
    private Integer channelReturnResult;

    /**
     * 渠道返回code
     */
    private String channelReturnCode;

    /**
     * 渠道返回消息
     */
    private String  channelReturnMsg;

    /**
     * 支付退款单编号， 自增
     */
    private Long refundId;
}
