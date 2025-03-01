package cn.iocoder.yudao.module.highway.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 项目管理 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProjectRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20702")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("项目编号")
    private String code;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("项目名称")
    private String pname;

    @Schema(description = "项目描述")
    @ExcelProperty("项目描述")
    private String description;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}