package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

// TODO 芋艿：字段优化
@ApiModel("交易订单分页 Request VO")
@Data
public class AppTradeOrderPageReqVO extends PageParam {

    @ApiModelProperty(value = "订单状态", example = "1", notes = "参见 TradeOrderStatusEnum 枚举")
    @InEnum(value = TradeOrderStatusEnum.class, message = "订单状态必须是 {value}")
    private Integer status;

}
