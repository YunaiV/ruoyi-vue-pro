package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

/**
 * 项目安排单 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ProjectScheduleBaseVO {

    @Schema(description = "项目 id", example = "31969")
    private Long projectId;

    @Schema(description = "报价单的名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotNull(message = "报价单的名字不能为空")
    private String name;

    @Schema(description = "状态, 待审批、已审批", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态, 待审批、已审批不能为空")
    private String status;

}
