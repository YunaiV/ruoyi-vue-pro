package cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.quantitydetail;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - FMS 数量金额明细账 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FmsLedgerQuantityDetailRespVO {

    public static final Integer ROW_TYPE_OPENING = 1;
    public static final Integer ROW_TYPE_VOUCHER = 2;
    public static final Integer ROW_TYPE_PERIOD_TOTAL = 3;
    public static final Integer ROW_TYPE_YEAR_TOTAL = 4;

    @Schema(description = "行类型", example = "2")
    private Integer rowType;

    @Schema(description = "分录编号", example = "1024")
    private Long entryId;

    @Schema(description = "科目编号", example = "1024")
    private Long subjectId;

    @Schema(description = "科目编码", example = "1405")
    private String subjectCode;

    @Schema(description = "科目名称", example = "库存商品")
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

    @Schema(description = "摘要", example = "采购商品")
    @ExcelProperty("摘要")
    private String digest;

    @Schema(description = "借方金额")
    @ExcelProperty("借方金额")
    private BigDecimal debitAmount;

    @Schema(description = "贷方金额")
    @ExcelProperty("贷方金额")
    private BigDecimal creditAmount;

    @Schema(description = "余额方向", example = "借")
    @ExcelProperty("方向")
    private String balanceDirection;

    @Schema(description = "余额")
    @ExcelProperty("结存金额")
    private BigDecimal balance;

    @Schema(description = "借方数量")
    @ExcelProperty("借方数量")
    private BigDecimal debitQuantity;

    @Schema(description = "贷方数量")
    @ExcelProperty("贷方数量")
    private BigDecimal creditQuantity;

    @Schema(description = "结存数量")
    @ExcelProperty("结存数量")
    private BigDecimal balanceQuantity;

    @Schema(description = "单价")
    @ExcelProperty("分录单价")
    private BigDecimal unitPrice;

    @ExcelProperty("期初单价")
    private BigDecimal openingUnitPrice;

    @ExcelProperty("本期单价")
    private BigDecimal periodUnitPrice;

    @ExcelProperty("累计单价")
    private BigDecimal yearUnitPrice;

    @ExcelProperty("期末单价")
    private BigDecimal endingUnitPrice;

    @Schema(description = "计量单位", example = "件")
    @ExcelProperty("单位")
    private String quantityUnit;

}
