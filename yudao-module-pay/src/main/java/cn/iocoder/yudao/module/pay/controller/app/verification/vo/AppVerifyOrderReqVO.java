package cn.iocoder.yudao.module.pay.controller.app.verification.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Schema(description = "用户 App - 订单数据验证 Request VO")
@Data
public class AppVerifyOrderReqVO {

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20240101001")
    @NotBlank(message = "订单号不能为空")
    private String orderId;

    @Schema(description = "待验证的订单数据（key-value）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "验证数据不能为空")
    private Map<String, Object> data;

}
