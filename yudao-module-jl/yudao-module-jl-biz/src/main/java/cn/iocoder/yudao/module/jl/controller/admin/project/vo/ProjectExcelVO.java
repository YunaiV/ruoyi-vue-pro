package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 项目管理 Excel VO
 *
 * @author 惟象科技
 */
@Data
public class ProjectExcelVO {

    @ExcelProperty("岗位ID")
    private Long id;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("销售线索 id")
    private Long salesleadId;

    @ExcelProperty("项目名字")
    private String name;

    @ExcelProperty("项目开展阶段")
    private String stage;

    @ExcelProperty("项目状态")
    private String status;

    @ExcelProperty("项目类型")
    private String type;

    @ExcelProperty("启动时间")
    private LocalDate startDate;

    @ExcelProperty("截止时间")
    private LocalDate endDate;

    @ExcelProperty("项目负责人")
    private Long managerId;

    @ExcelProperty("参与者 ids，数组")
    private String participants;

    @ExcelProperty("销售 id")
    private Long salesId;

    @ExcelProperty("销售 id")
    private Long customerId;

}
