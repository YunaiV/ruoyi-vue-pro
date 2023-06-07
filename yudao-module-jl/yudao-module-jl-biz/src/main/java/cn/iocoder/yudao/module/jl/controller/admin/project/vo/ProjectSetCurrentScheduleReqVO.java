package cn.iocoder.yudao.module.jl.controller.admin.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 设置项目主安排单 Request VO")
@Data
@ToString(callSuper = true)
public class ProjectSetCurrentScheduleReqVO {

    @Schema(description = "scheduleId", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "schedule id 不能为空")
    private Long scheduleId;

    @Schema(description = "projectId", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "project id 不能为空")
    private Long projectId;

}
