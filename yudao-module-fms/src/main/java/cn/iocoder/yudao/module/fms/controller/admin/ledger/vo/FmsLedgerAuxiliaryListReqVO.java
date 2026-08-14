package cn.iocoder.yudao.module.fms.controller.admin.ledger.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "管理后台 - FMS 核算项目账簿列表查询 Request VO")
@Data
public class FmsLedgerAuxiliaryListReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "开始会计期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08")
    @NotNull(message = "开始会计期间不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "开始会计期间格式不正确")
    private String startMonth;

    @Schema(description = "结束会计期间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-12")
    @NotNull(message = "结束会计期间不能为空")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "结束会计期间格式不正确")
    private String endMonth;

    @Schema(description = "辅助核算类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "辅助核算类别编号不能为空")
    private Long auxiliaryTypeId;

    @Schema(description = "科目编号", example = "1024")
    private Long subjectId;

    @Schema(description = "辅助核算项目编号", example = "1024")
    private Long auxiliaryItemId;

}
