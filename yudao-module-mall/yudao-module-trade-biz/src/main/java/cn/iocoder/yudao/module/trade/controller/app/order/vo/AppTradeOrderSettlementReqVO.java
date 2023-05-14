package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "用户 App - 交易订单结算 Request VO")
@Data
public class AppTradeOrderSettlementReqVO {

    @Schema(description = "收件地址编号", example = "1")
    private Long addressId;

    @Schema(description = "优惠劵编号", example = "1024")
    private Long couponId;

    @Schema(description = "购物车项的编号数组", required = true, example = "true")
    @NotEmpty(message = "购物车项不能为空")
    private List<Long> cartIds;

}
