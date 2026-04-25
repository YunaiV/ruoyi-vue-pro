package cn.iocoder.yudao.module.trade.controller.app.wholesale.pay.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "用户 App - 批发订单发起退款 Request VO")
@Data
public class AppWholesaleRefundReqVO {

    @Schema(description = "商户订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "WH_1001_1715000000000")
    @NotBlank(message = "商户订单号不能为空")
    private String merchantOrderId;

    @Schema(description = "商户退款单号（全局唯一）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "REFUND_WH_1001_1715000001000")
    @NotBlank(message = "商户退款单号不能为空")
    private String merchantRefundId;

    @Schema(description = "退款金额（分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "500000")
    @NotNull(message = "退款金额不能为空")
    @Min(value = 1, message = "退款金额必须大于 0")
    private Integer refundPrice;

    @Schema(description = "退款原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "协商退款")
    @NotBlank(message = "退款原因不能为空")
    private String reason;

    @Schema(description = "用户 IP", example = "192.168.1.100")
    private String userIp;

}
