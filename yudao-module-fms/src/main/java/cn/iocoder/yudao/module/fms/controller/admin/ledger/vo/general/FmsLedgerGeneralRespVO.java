package cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.general;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - FMS 总账 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FmsLedgerGeneralRespVO {

    @Schema(description = "行类型", example = "2")
    private Integer rowType;

    @Schema(description = "科目编号", example = "1024")
    private Long subjectId;

    @Schema(description = "科目编码", example = "1001")
    @ExcelProperty("科目编码")
    private String subjectCode;

    @Schema(description = "科目名称", example = "库存现金")
    @ExcelProperty("科目名称")
    private String subjectName;

    @Schema(description = "会计期间", example = "2026-08")
    @ExcelProperty("期间")
    private String period;

    @Schema(description = "摘要", example = "本期合计")
    @ExcelProperty("摘要")
    private String digest;

    @Schema(description = "借方金额")
    @ExcelProperty("借方")
    private BigDecimal debitAmount;

    @Schema(description = "贷方金额")
    @ExcelProperty("贷方")
    private BigDecimal creditAmount;

    @Schema(description = "余额方向", example = "借")
    @ExcelProperty("方向")
    private String balanceDirection;

    @Schema(description = "余额")
    @ExcelProperty("余额")
    private BigDecimal balance;

}
