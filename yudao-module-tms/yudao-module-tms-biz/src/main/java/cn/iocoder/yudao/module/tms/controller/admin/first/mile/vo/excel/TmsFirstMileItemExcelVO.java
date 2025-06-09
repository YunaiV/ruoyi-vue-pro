package cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 头程单子表 Excel VO
 */
@Data
public class TmsFirstMileItemExcelVO {

    @ExcelProperty("货物名称")
    private String cargoName;

    @ExcelProperty("货物数量")
    private Integer cargoQuantity;

    @ExcelProperty("货物重量(kg)")
    private BigDecimal cargoWeight;

    @ExcelProperty("货物体积(m³)")
    private BigDecimal cargoVolume;

    @ExcelProperty("货物价值")
    private BigDecimal cargoValue;

    @ExcelProperty("备注")
    private String remark;
} 