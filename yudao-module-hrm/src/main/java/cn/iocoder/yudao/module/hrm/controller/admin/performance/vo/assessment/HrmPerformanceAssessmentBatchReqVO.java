package cn.iocoder.yudao.module.hrm.controller.admin.performance.vo.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 绩效计划增删员工 Request VO")
@Data
public class HrmPerformanceAssessmentBatchReqVO {

    @Schema(description = "绩效计划编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "绩效计划不能为空")
    private Long planId;

    @Schema(description = "员工编号列表", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[1024, 2048]")
    @NotEmpty(message = "员工不能为空")
    private List<Long> employeeIds;

}
