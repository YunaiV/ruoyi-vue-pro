package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 项目管理 Excel 导出 Request VO，参数和 ProjectPageReqVO 是一致的")
@Data
public class ProjectExportReqVO {

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "销售线索 id", example = "15320")
    private Long salesleadId;

    @Schema(description = "项目名字", example = "赵六")
    private String name;

    @Schema(description = "项目开展阶段")
    private String stage;

    @Schema(description = "项目状态", example = "1")
    private String status;

    @Schema(description = "项目类型", example = "1")
    private String type;

    @Schema(description = "启动时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] startDate;

    @Schema(description = "截止时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] endDate;

    @Schema(description = "项目负责人", example = "6150")
    private Long managerId;

    @Schema(description = "参与者 ids，数组")
    private String participants;

    @Schema(description = "销售 id", example = "16310")
    private Long salesId;

    @Schema(description = "销售 id", example = "8556")
    private Long customerId;

}
