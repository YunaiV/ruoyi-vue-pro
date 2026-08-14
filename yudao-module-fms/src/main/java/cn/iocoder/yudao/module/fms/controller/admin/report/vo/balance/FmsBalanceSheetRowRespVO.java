package cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - FMS 资产负债表行 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FmsBalanceSheetRowRespVO {

    @Schema(description = "行编号", example = "1")
    private Integer rowId;

    @Schema(description = "资产项目配置编号", example = "1024")
    private Long assetId;

    @Schema(description = "资产项目名称", example = "货币资金")
    @ExcelProperty("资产")
    private String assetName;

    @Schema(description = "资产项目行次", example = "1")
    @ExcelProperty("行次")
    private Integer assetRowNo;

    @Schema(description = "资产项目期末余额", example = "200.00")
    @ExcelProperty("期末余额")
    private BigDecimal assetClosingAmount;

    @Schema(description = "资产项目年初余额", example = "100.00")
    @ExcelProperty("年初余额")
    private BigDecimal assetOpeningAmount;

    @Schema(description = "资产项目层级", example = "2")
    private Integer assetLevel;

    @Schema(description = "资产项目是否可编辑", example = "true")
    private Boolean assetEditable;

    @Schema(description = "资产项目公式")
    private String assetFormula;

    @Schema(description = "负债和所有者权益项目配置编号", example = "1024")
    private Long liabilityId;

    @Schema(description = "负债和所有者权益项目名称", example = "短期借款")
    @ExcelProperty("负债和所有者权益")
    private String liabilityName;

    @Schema(description = "负债和所有者权益项目行次", example = "31")
    @ExcelProperty("行次")
    private Integer liabilityRowNo;

    @Schema(description = "负债和所有者权益项目期末余额", example = "200.00")
    @ExcelProperty("期末余额")
    private BigDecimal liabilityClosingAmount;

    @Schema(description = "负债和所有者权益项目年初余额", example = "100.00")
    @ExcelProperty("年初余额")
    private BigDecimal liabilityOpeningAmount;

    @Schema(description = "负债和所有者权益项目层级", example = "2")
    private Integer liabilityLevel;

    @Schema(description = "负债和所有者权益项目是否可编辑", example = "true")
    private Boolean liabilityEditable;

    @Schema(description = "负债和所有者权益项目公式")
    private String liabilityFormula;

}
