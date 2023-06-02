package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 项目报价 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class ProjectQuoteExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("销售线索 id")
    private Long salesleadId;

    @ExcelProperty("项目 id")
    private Long projectId;

    @ExcelProperty("报价单的名字")
    private String name;

    @ExcelProperty("方案 URL")
    private String reportUrl;

    @ExcelProperty("折扣(100: 无折扣, 98: 98折)")
    private Integer discount;

    @ExcelProperty("状态, 已提交、已作废、已采用")
    private String status;

}
