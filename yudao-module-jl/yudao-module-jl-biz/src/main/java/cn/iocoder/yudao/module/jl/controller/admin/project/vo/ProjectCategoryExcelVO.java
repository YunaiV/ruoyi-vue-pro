package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 项目的实验名目 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class ProjectCategoryExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("报价 id")
    private Long quoteId;

    @ExcelProperty("安排单 id")
    private Long scheduleId;

    @ExcelProperty("类型，报价/安排单")
    private String type;

    @ExcelProperty("名目的实验类型，动物/细胞/分子等")
    private String categoryType;

    @ExcelProperty("实验名目库的名目 id")
    private Long categoryId;

    @ExcelProperty("实验人员")
    private Long operatorId;

    @ExcelProperty("客户需求")
    private String demand;

    @ExcelProperty("干扰项")
    private String interference;

    @ExcelProperty("依赖项(json数组多个)")
    private String dependIds;

    @ExcelProperty("实验名目名字")
    private String name;

    @ExcelProperty("备注")
    private String mark;

}
