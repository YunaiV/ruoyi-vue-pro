package cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * FMS 初始余额 Excel VO
 *
 * @author 芋道源码
 */
@Data
@ExcelIgnoreUnannotated
public class FmsInitialBalanceExcelVO {

    /**
     * Excel 行号
     */
    private Integer rowNumber;
    /**
     * 科目编码
     */
    @ExcelProperty("科目编码")
    private String subjectCode;
    /**
     * 科目名称
     */
    @ExcelProperty("科目名称")
    private String subjectName;
    /**
     * 余额方向名称
     */
    @ExcelProperty("方向")
    private String directionName;
    /**
     * 辅助核算项目，格式为“类别:名称/类别:名称”
     */
    @ExcelProperty("辅助核算项目")
    private String auxiliaryItems;
    /**
     * 科目类型
     */
    private Integer subjectType;
    /**
     * 是否启用数量核算
     */
    private Boolean quantityAccounting;
    /**
     * 是否启用辅助核算
     */
    private Boolean auxiliaryAccounting;
    /**
     * 期初数量
     */
    @ExcelProperty("期初数量")
    private BigDecimal openingQuantity;
    /**
     * 期初金额
     */
    @ExcelProperty("期初金额")
    private BigDecimal openingAmount;
    /**
     * 本年累计借方数量
     */
    @ExcelProperty("本年累计借方数量")
    private BigDecimal yearDebitQuantity;
    /**
     * 本年累计借方金额
     */
    @ExcelProperty("本年累计借方金额")
    private BigDecimal yearDebitAmount;
    /**
     * 本年累计贷方数量
     */
    @ExcelProperty("本年累计贷方数量")
    private BigDecimal yearCreditQuantity;
    /**
     * 本年累计贷方金额
     */
    @ExcelProperty("本年累计贷方金额")
    private BigDecimal yearCreditAmount;
    /**
     * 年初数量
     */
    @ExcelProperty("年初数量")
    private BigDecimal yearOpeningQuantity;
    /**
     * 年初金额
     */
    @ExcelProperty("年初金额")
    private BigDecimal yearOpeningAmount;
    /**
     * 实际损益发生数量
     */
    @ExcelProperty("实际损益发生数量")
    private BigDecimal profitLossQuantity;
    /**
     * 实际损益发生金额
     */
    @ExcelProperty("实际损益发生金额")
    private BigDecimal profitLossAmount;

}
