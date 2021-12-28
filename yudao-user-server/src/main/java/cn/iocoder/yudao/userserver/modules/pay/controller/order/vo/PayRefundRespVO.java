package cn.iocoder.yudao.userserver.modules.pay.controller.order.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@ApiModel("提交退款订单 Response VO")
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefundRespVO {

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
