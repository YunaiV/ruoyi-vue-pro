package cn.iocoder.yudao.module.pay.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - 支付订单提交 Response VO")
@Data
public class PayOrderSubmitRespVO {

    @Schema(description = "展示模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "url") // 参见 PayDisplayModeEnum 枚举
    private String displayMode;

    @Schema(description = "展示内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String displayContent;

}
