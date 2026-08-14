package cn.iocoder.yudao.module.fms.controller.admin.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - FMS 报表公式项 Response VO")
@Data
public class FmsReportFormulaRespVO {

    @Schema(description = "科目编号", example = "1024")
    private Long subjectId;

    @Schema(description = "科目名称", example = "库存现金")
    private String subjectName;

    @Schema(description = "科目编码", example = "1001")
    private String subjectNumber;

    @Schema(description = "运算符", example = "+")
    private String operator;

    @Schema(description = "取数规则", example = "0")
    private Integer rules;

    @Schema(description = "期初金额", example = "100.00")
    private BigDecimal openingAmount;

    @Schema(description = "期末金额", example = "200.00")
    private BigDecimal closingAmount;

    @Schema(description = "本期金额", example = "100.00")
    private BigDecimal currentAmount;

    @Schema(description = "本年累计金额", example = "200.00")
    private BigDecimal yearAmount;

}
