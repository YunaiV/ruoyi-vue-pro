package cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.auxiliarybalance;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema(description = "管理后台 - FMS 核算项目余额 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FmsLedgerAuxiliaryBalanceRespVO {

    @Schema(description = "辅助核算项目编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long auxiliaryItemId;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("编码")
    private String code;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("项目名称")
    private String name;

    @Schema(description = "期初借方余额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("期初借方")
    private BigDecimal openingDebitAmount;

    @Schema(description = "期初贷方余额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("期初贷方")
    private BigDecimal openingCreditAmount;

    @Schema(description = "本期借方发生额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本期借方")
    private BigDecimal periodDebitAmount;

    @Schema(description = "本期贷方发生额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本期贷方")
    private BigDecimal periodCreditAmount;

    @Schema(description = "本年累计借方发生额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本年累计借方")
    private BigDecimal yearDebitAmount;

    @Schema(description = "本年累计贷方发生额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本年累计贷方")
    private BigDecimal yearCreditAmount;

    @Schema(description = "期末借方余额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("期末借方")
    private BigDecimal endingDebitAmount;

    @Schema(description = "期末贷方余额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("期末贷方")
    private BigDecimal endingCreditAmount;

}
