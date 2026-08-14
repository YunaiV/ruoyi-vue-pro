package cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountset;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsAccountingStandardEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsCurrencyPresetEnum;
import cn.iocoder.yudao.module.fms.enums.ledger.FmsLedgerBalanceModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - FMS 账套初始化 Request VO")
@Data
public class FmsAccountSetInitializeReqVO {

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "本位币编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "RMB")
    @NotBlank(message = "本位币不能为空")
    @InEnum(value = FmsCurrencyPresetEnum.class, message = "本位币必须是 {value}")
    private String currencyCode;

    @Schema(description = "启用期间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "启用期间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "会计制度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "会计制度不能为空")
    @InEnum(value = FmsAccountingStandardEnum.class, message = "会计制度必须是 {value}")
    private Integer standard;

    @Schema(description = "科目层级", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    @NotNull(message = "科目层级不能为空")
    @Min(value = 1, message = "科目层级不能小于 1")
    @Max(value = 8, message = "科目层级不能大于 8")
    private Integer level;

    @Schema(description = "科目编码规则", requiredMode = Schema.RequiredMode.REQUIRED, example = "4-2-2-2")
    @NotBlank(message = "科目编码规则不能为空")
    @Size(max = 64, message = "科目编码规则不能超过 64 个字符")
    private String subjectCodeRule;

    @Schema(description = "账簿余额方向模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账簿余额方向模式不能为空")
    @InEnum(FmsLedgerBalanceModeEnum.class)
    private Integer ledgerBalanceMode;

}
