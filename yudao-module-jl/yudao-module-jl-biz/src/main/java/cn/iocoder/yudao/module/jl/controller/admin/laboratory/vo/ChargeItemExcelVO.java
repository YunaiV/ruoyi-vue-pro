package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 实验收费项 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class ChargeItemExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("收费类型")
    private String type;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("成本价")
    private Long costPrice;

    @ExcelProperty("建议销售价")
    private Long suggestedSellingPrice;

    @ExcelProperty("备注")
    private String mark;

    @ExcelProperty("收费的标准/规则")
    private String feeStandard;

}
