package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 订单发货 Request VO")
@Data
public class TradeOrderDeliveryReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "订单编号不能为空")
    private Long id;

    @Schema(description = "发货物流公司编号", example = "1")
    @NotNull(message = "发货物流公司不能为空")
    private Long logisticsId;

    @Schema(description = "发货物流单号", example = "SF123456789")
    private String logisticsNo;

}
