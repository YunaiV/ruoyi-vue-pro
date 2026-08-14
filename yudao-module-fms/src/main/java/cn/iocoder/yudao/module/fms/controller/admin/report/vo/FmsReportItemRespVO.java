package cn.iocoder.yudao.module.fms.controller.admin.report.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - FMS 财务报表项目 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FmsReportItemRespVO {

    @Schema(description = "配置编号", example = "1024")
    private Long id;

    @Schema(description = "项目名称", example = "货币资金")
    @ExcelProperty("项目")
    private String name;

    @Schema(description = "行次", example = "1")
    @ExcelProperty("行次")
    private Integer rowNo;

    @Schema(description = "层级", example = "2")
    private Integer level;

    @Schema(description = "是否可编辑", example = "true")
    private Boolean editable;

    @Schema(description = "公式")
    private String formula;

    @Schema(description = "期初金额", example = "100.00")
    private BigDecimal openingAmount;

    @Schema(description = "期末金额", example = "200.00")
    private BigDecimal closingAmount;

    @Schema(description = "本年累计金额", example = "200.00")
    @ExcelProperty("本年累计金额")
    private BigDecimal yearAmount;

    @Schema(description = "本期金额", example = "100.00")
    @ExcelProperty("本期金额")
    private BigDecimal currentAmount;

}
