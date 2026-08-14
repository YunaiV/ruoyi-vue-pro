package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - FMS 凭证汇总 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FmsVoucherStatisticsRespVO {

    @Schema(description = "科目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long subjectId;

    @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @ExcelProperty("科目编码")
    private String subjectCode;

    @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "库存现金")
    @ExcelProperty("科目名称")
    private String subjectName;

    @Schema(description = "科目级次", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer level;

    @Schema(description = "借方金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @ExcelProperty("借方金额")
    private BigDecimal debitAmount;

    @Schema(description = "贷方金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @ExcelProperty("贷方金额")
    private BigDecimal creditAmount;

}
