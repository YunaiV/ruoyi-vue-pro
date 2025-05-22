package cn.iocoder.yudao.module.pay.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 支付订单提交 Response VO")
@Data
public class PayOrderSubmitRespVO {

    @Schema(description = "支付状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10") // 参见 PayOrderStatusEnum 枚举
    private Integer status;

    @Schema(description = "展示模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "url") // 参见 PayDisplayModeEnum 枚举
    private String displayMode;
    @Schema(description = "展示内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String displayContent;

}
