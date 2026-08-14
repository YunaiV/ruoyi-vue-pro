package cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.detail;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FMS 明细账导出 Response VO
 *
 * @author 芋道源码
 */
@Data
@ExcelIgnoreUnannotated
public class FmsLedgerDetailExportRespVO {

    @ExcelProperty("科目")
    private String subject;

    @ExcelProperty("日期")
    private LocalDate accountDate;

    @ExcelProperty("凭证字号")
    private String voucherNumber;

    @ExcelProperty("摘要")
    private String digest;

    @ExcelProperty("借方")
    private BigDecimal debitAmount;

    @ExcelProperty("贷方")
    private BigDecimal creditAmount;

    @ExcelProperty("方向")
    private String balanceDirection;

    @ExcelProperty("余额")
    private BigDecimal balance;

}
