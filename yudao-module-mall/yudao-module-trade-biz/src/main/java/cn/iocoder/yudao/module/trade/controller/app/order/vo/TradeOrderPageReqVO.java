package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "交易订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TradeOrderPageReqVO extends PageParam {

    @Schema(description = "订单状态-参见 TradeOrderStatusEnum 枚举", example = "1")
    private Integer orderStatus;

}
