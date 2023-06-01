package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 实验名目的擅长人员 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class CategorySkillUserExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("实验名目 id")
    private Long categoryId;

    @ExcelProperty("实验人员 id")
    private Long userId;

}
