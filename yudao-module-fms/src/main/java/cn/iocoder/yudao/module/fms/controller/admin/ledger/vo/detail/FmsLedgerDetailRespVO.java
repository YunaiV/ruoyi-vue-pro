package cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.detail;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - FMS 账簿明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FmsLedgerDetailRespVO {

    public static final Integer ROW_TYPE_OPENING = 1;
    public static final Integer ROW_TYPE_VOUCHER = 2;
    public static final Integer ROW_TYPE_PERIOD_TOTAL = 3;
    public static final Integer ROW_TYPE_YEAR_TOTAL = 4;
    public static final Integer ROW_TYPE_ENDING = 5;

    @Schema(description = "行类型", example = "2")
    private Integer rowType;

    @Schema(description = "分录编号", example = "1024")
    private Long entryId;

    @Schema(description = "分录科目编号", example = "1024")
    private Long entrySubjectId;

    @Schema(description = "科目编号", example = "1024")
    private Long subjectId;

    @Schema(description = "科目编码", example = "1001")
    private String subjectCode;

    @Schema(description = "科目名称", example = "库存现金")
    private String subjectName;

    @Schema(description = "会计期间", example = "2026-08")
    private String period;

    @Schema(description = "日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ExcelProperty("日期")
    private LocalDate accountDate;

    @Schema(description = "凭证编号", example = "1024")
    private Long voucherId;

    @Schema(description = "凭证字号", example = "记-1")
    @ExcelProperty("凭证字号")
    private String voucherNumber;

    @Schema(description = "摘要", example = "报销办公用品")
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

    @Schema(description = "借方数量")
    private BigDecimal debitQuantity;

    @Schema(description = "贷方数量")
    private BigDecimal creditQuantity;

    @Schema(description = "结存数量")
    private BigDecimal balanceQuantity;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "计量单位", example = "件")
    private String quantityUnit;

    @Schema(description = "多栏账科目金额")
    private java.util.Map<Long, BigDecimal> columnAmounts;

}
