package cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeparameter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - FMS 财务参数 Response VO")
@Data
public class FmsFinanceParameterRespVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long accountSetId;

    @Schema(description = "科目层级", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Integer level;

    @Schema(description = "科目编码规则", requiredMode = Schema.RequiredMode.REQUIRED, example = "4-2-2-2")
    private String subjectCodeRule;

    @Schema(description = "账簿余额方向模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer ledgerBalanceMode;

    @Schema(description = "结账前是否要求凭证审核", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean voucherReviewRequired;

}
