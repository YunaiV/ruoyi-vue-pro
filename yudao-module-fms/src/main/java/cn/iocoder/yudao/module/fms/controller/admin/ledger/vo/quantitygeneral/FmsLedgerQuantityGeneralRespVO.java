package cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.quantitygeneral;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - FMS 数量金额总账 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FmsLedgerQuantityGeneralRespVO {

    @Schema(description = "科目编号", example = "1024")
    private Long subjectId;

    @Schema(description = "科目编码", example = "1405")
    @ExcelProperty({"科目", "编码"})
    private String subjectCode;

    @Schema(description = "科目名称", example = "库存商品")
    @ExcelProperty({"科目", "名称"})
    private String subjectName;

    @Schema(description = "科目级次", example = "1")
    private Integer level;

    @Schema(description = "是否启用数量核算")
    private Boolean quantityAccounting;

    @Schema(description = "计量单位", example = "件")
    @ExcelProperty({"基础信息", "单位"})
    private String quantityUnit;

    @Schema(description = "期初余额方向", example = "借")
    @ExcelProperty({"期初余额", "方向"})
    private String openingBalanceDirection;

    @Schema(description = "期初数量")
    @ExcelProperty({"期初余额", "数量"})
    private BigDecimal openingQuantity;

    @Schema(description = "期初单价")
    @ExcelProperty({"期初余额", "单价"})
    private BigDecimal openingUnitPrice;

    @Schema(description = "期初借方余额")
    @ExcelIgnore
    private BigDecimal openingDebitAmount;

    @Schema(description = "期初贷方余额")
    @ExcelIgnore
    private BigDecimal openingCreditAmount;

    @ExcelProperty({"期初余额", "金额"})
    private BigDecimal openingAmount;

    @Schema(description = "本期借方数量")
    @ExcelProperty({"本期借方", "数量"})
    private BigDecimal periodDebitQuantity;

    @Schema(description = "本期借方发生额")
    @ExcelProperty({"本期借方", "金额"})
    private BigDecimal periodDebitAmount;

    @Schema(description = "本期贷方数量")
    @ExcelProperty({"本期贷方", "数量"})
    private BigDecimal periodCreditQuantity;

    @Schema(description = "本期贷方发生额")
    @ExcelProperty({"本期贷方", "金额"})
    private BigDecimal periodCreditAmount;

    @Schema(description = "本年累计借方数量")
    @ExcelProperty({"本年累计借方", "数量"})
    private BigDecimal yearDebitQuantity;

    @Schema(description = "本年累计借方发生额")
    @ExcelProperty({"本年累计借方", "金额"})
    private BigDecimal yearDebitAmount;

    @Schema(description = "本年累计贷方数量")
    @ExcelProperty({"本年累计贷方", "数量"})
    private BigDecimal yearCreditQuantity;

    @Schema(description = "本年累计贷方发生额")
    @ExcelProperty({"本年累计贷方", "金额"})
    private BigDecimal yearCreditAmount;

    @Schema(description = "期末余额方向", example = "借")
    @ExcelProperty({"期末余额", "方向"})
    private String endingBalanceDirection;

    @Schema(description = "期末数量")
    @ExcelProperty({"期末余额", "数量"})
    private BigDecimal endingQuantity;

    @Schema(description = "期末单价")
    @ExcelProperty({"期末余额", "单价"})
    private BigDecimal endingUnitPrice;

    @Schema(description = "期末借方余额")
    @ExcelIgnore
    private BigDecimal endingDebitAmount;

    @Schema(description = "期末贷方余额")
    @ExcelIgnore
    private BigDecimal endingCreditAmount;

    @ExcelProperty({"期末余额", "金额"})
    private BigDecimal endingAmount;

    @Schema(description = "下级科目")
    private List<FmsLedgerQuantityGeneralRespVO> children = new ArrayList<>();

}
