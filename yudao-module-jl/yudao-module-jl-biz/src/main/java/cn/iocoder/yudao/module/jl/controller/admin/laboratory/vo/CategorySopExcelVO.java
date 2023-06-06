package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 实验名目的操作SOP Excel VO
 *
 * @author 惟象科技
 */
@Data
public class CategorySopExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("实验名目 id")
    private Long categoryId;

    @ExcelProperty("操作步骤的内容")
    private String content;

    @ExcelProperty("步骤序号")
    private Integer step;

    @ExcelProperty("注意事项")
    private String mark;

    @ExcelProperty("依赖项(json数组多个)")
    private String dependIds;

}
