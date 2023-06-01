package cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 实验名目的参考资料 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class CategoryReferenceExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("实验名目 id")
    private Long categoryId;

    @ExcelProperty("文件名")
    private String name;

    @ExcelProperty("操作步骤的内容")
    private String url;

    @ExcelProperty("类型(文献、结果参考、交付标准)")
    private String type;

}
