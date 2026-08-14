package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.format.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - FMS 凭证导出 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FmsVoucherExportRespVO {

    @Schema(description = "凭证日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("日期")
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDateTime voucherTime;

    @Schema(description = "凭证字", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("凭证字")
    private String voucherWordName;

    @Schema(description = "凭证号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("凭证号")
    private Integer voucherNumber;

    @Schema(description = "摘要", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("摘要")
    private String digest;

    @Schema(description = "科目代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("科目代码")
    private String subjectCode;

    @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("科目名称")
    private String subjectName;

    @Schema(description = "借方金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("借方金额")
    private BigDecimal debitAmount;

    @Schema(description = "贷方金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("贷方金额")
    private BigDecimal creditAmount;

    @Schema(description = "附件数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("附件数")
    private Integer attachmentCount;

    @Schema(description = "制单人")
    @ExcelProperty("制单人")
    private String creatorUserName;

    @Schema(description = "审核人")
    @ExcelProperty("审核人")
    private String reviewerUserName;

}
