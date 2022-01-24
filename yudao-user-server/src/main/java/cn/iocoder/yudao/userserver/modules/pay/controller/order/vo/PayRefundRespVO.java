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
     * 支付退款单编号， 自增
     */
    private Long refundId;
}
