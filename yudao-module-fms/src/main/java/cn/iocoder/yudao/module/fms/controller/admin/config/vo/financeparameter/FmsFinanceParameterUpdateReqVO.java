package cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeparameter;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.ledger.FmsLedgerBalanceModeEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsAccountingStandardEnum;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - FMS 财务参数更新 Request VO")
@Data
public class FmsFinanceParameterUpdateReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "会计制度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "会计制度不能为空")
    @InEnum(FmsAccountingStandardEnum.class)
    @DiffLogField(name = "会计制度")
    private Integer standard;

    @Schema(description = "科目层级", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    @NotNull(message = "科目层级不能为空")
    @Min(value = 1, message = "科目层级不能小于 1")
    @Max(value = 8, message = "科目层级不能大于 8")
    @DiffLogField(name = "科目层级")
    private Integer level;

    @Schema(description = "科目编码规则", requiredMode = Schema.RequiredMode.REQUIRED, example = "4-2-2-2")
    @NotBlank(message = "科目编码规则不能为空")
    @Size(max = 64, message = "科目编码规则不能超过 64 个字符")
    @DiffLogField(name = "科目编码规则")
    private String subjectCodeRule;

    @Schema(description = "账簿余额方向模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账簿余额方向模式不能为空")
    @InEnum(FmsLedgerBalanceModeEnum.class)
    @DiffLogField(name = "账簿余额方向")
    private Integer ledgerBalanceMode;

    @Schema(description = "结账前是否要求凭证审核", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "结账前凭证审核设置不能为空")
    @DiffLogField(name = "凭证审核后才允许结账")
    private Boolean voucherReviewRequired;

}
