package cn.iocoder.yudao.module.trade.controller.app.wholesale.pay.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "用户 App - 批发订单发起支付 Request VO")
@Data
public class AppWholesalePaySubmitReqVO {

    @Schema(description = "商户订单号（合同编号）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "WH_1001_1715000000000")
    @NotBlank(message = "商户订单号不能为空")
    private String merchantOrderId;

    @Schema(description = "商品标题（最长 32 字）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "B2B批发采购-春季新款")
    @NotBlank(message = "商品标题不能为空")
    private String subject;

    @Schema(description = "支付金额（分）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "1000000")
    @NotNull(message = "支付金额不能为空")
    @Min(value = 1, message = "支付金额必须大于 0")
    private Integer priceInFen;

    @Schema(description = "用户 IP（选填，网关可自动获取）", example = "192.168.1.100")
    private String userIp;

}
