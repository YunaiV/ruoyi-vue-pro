package cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - FMS 科目余额 Response VO")
@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
public class FmsLedgerSubjectBalanceRespVO {

    public static final int NODE_TYPE_SUBJECT = 1;
    public static final int NODE_TYPE_AUXILIARY_COMBINATION = 2;

    @Schema(description = "节点唯一键", example = "S:1024")
    private String nodeKey;

    @Schema(description = "节点类型", example = "1")
    private Integer nodeType;

    @Schema(description = "科目编号", example = "1024")
    private Long subjectId;

    @Schema(description = "辅助核算组合编号", example = "2048")
    private Long assistCombinationId;

    @Schema(description = "科目编码", example = "1001")
    @ExcelProperty("科目编码")
    private String subjectCode;

    @Schema(description = "科目名称", example = "库存现金")
    @ExcelProperty("科目名称")
    private String subjectName;

    @Schema(description = "科目级次", example = "1")
    private Integer level;

    @Schema(description = "是否启用数量核算")
    private Boolean quantityAccounting;

    @Schema(description = "计量单位", example = "件")
    private String quantityUnit;

    @Schema(description = "期初借方余额")
    @ExcelProperty("期初借方")
    private BigDecimal openingDebitAmount;

    @Schema(description = "期初贷方余额")
    @ExcelProperty("期初贷方")
    private BigDecimal openingCreditAmount;

    @Schema(description = "期初余额方向", example = "借")
    private String openingBalanceDirection;

    @Schema(description = "期初数量")
    private BigDecimal openingQuantity;

    @Schema(description = "期初单价")
    private BigDecimal openingUnitPrice;

    @Schema(description = "本期借方发生额")
    @ExcelProperty("本期借方")
    private BigDecimal periodDebitAmount;

    @Schema(description = "本期贷方发生额")
    @ExcelProperty("本期贷方")
    private BigDecimal periodCreditAmount;

    @Schema(description = "本期借方数量")
    private BigDecimal periodDebitQuantity;

    @Schema(description = "本期贷方数量")
    private BigDecimal periodCreditQuantity;

    @Schema(description = "本年累计借方发生额")
    @ExcelProperty("本年累计借方")
    private BigDecimal yearDebitAmount;

    @Schema(description = "本年累计贷方发生额")
    @ExcelProperty("本年累计贷方")
    private BigDecimal yearCreditAmount;

    @Schema(description = "本年累计借方数量")
    private BigDecimal yearDebitQuantity;

    @Schema(description = "本年累计贷方数量")
    private BigDecimal yearCreditQuantity;

    @Schema(description = "期末借方余额")
    @ExcelProperty("期末借方")
    private BigDecimal endingDebitAmount;

    @Schema(description = "期末贷方余额")
    @ExcelProperty("期末贷方")
    private BigDecimal endingCreditAmount;

    @Schema(description = "期末余额方向", example = "借")
    private String endingBalanceDirection;

    @Schema(description = "期末数量")
    private BigDecimal endingQuantity;

    @Schema(description = "期末单价")
    private BigDecimal endingUnitPrice;

    @Schema(description = "下级科目")
    private List<FmsLedgerSubjectBalanceRespVO> children = new ArrayList<>();

}
