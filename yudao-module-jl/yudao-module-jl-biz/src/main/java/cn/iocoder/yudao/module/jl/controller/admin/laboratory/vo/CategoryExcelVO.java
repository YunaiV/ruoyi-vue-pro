package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 实验名目 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class CategoryExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("名字")
    private String name;

    @ExcelProperty("技术难度")
    private String difficultyLevel;

    @ExcelProperty("重要备注说明")
    private String mark;

    @ExcelProperty("类型")
    private String type;

}
