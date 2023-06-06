package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 实验物资 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class SupplyExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("物资名称")
    private String name;

    @ExcelProperty("物资类型")
    private String type;

    @ExcelProperty("备注")
    private String mark;

}
