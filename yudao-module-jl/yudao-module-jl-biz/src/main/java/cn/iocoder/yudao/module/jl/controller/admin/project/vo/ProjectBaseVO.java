package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 项目管理 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ProjectBaseVO {

    @Schema(description = "销售线索 id", example = "15320")
    private Long salesleadId;

    @Schema(description = "项目名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotNull(message = "项目名字不能为空")
    private String name;

    @Schema(description = "项目开展阶段")
    private String stage;

    @Schema(description = "项目状态", example = "1")
    private String status;

    @Schema(description = "项目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目类型不能为空")
    private String type;

    @Schema(description = "启动时间")
    private LocalDate startDate;

    @Schema(description = "截止时间")
    private LocalDate endDate;

    @Schema(description = "项目负责人", example = "6150")
    private Long managerId;

    @Schema(description = "参与者 ids，数组")
    private String participants;

    @Schema(description = "销售 id", example = "16310")
    private Long salesId;

    @Schema(description = "销售 id", example = "8556")
    private Long customerId;

}
