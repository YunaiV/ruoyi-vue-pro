package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(title = "交易订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TradeOrderPageReqVO extends PageParam {

    @Schema(title = "订单状态", example = "1", description = "参见 TradeOrderStatusEnum 枚举")
    private Integer orderStatus;

}
