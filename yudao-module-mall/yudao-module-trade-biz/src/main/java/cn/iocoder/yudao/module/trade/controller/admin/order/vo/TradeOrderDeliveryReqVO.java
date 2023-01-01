package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 订单发货 Request VO")
@Data
public class TradeOrderDeliveryReqVO {

    @ApiModelProperty(name = "订单编号", required = true, example = "1024")
    @NotNull(message = "订单编号不能为空")
    private Long id;

    @ApiModelProperty(name = "发货物流公司编号", required = true, example = "1")
    @NotNull(message = "发货物流公司编号不能为空")
    private Long logisticsId;

    @ApiModelProperty(name = "发货物流单号", required = true, example = "SF123456789")
    @NotEmpty(message = "发货物流单号不能为空")
    private String logisticsNo;

}
