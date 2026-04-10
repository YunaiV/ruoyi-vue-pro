package cn.iocoder.yudao.module.pay.controller.admin.transfer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 发起转账 Response VO")
@Data
public class PayTransferCreateRespVO {

    @Schema(description = "转账单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "转账状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1") // 参见 PayTransferStatusEnum 枚举
    private Integer status;

}
