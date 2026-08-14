package cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.assessment;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - HRM 员工端我的绩效分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HrmPortalPerformanceAssessmentPageReqVO extends PageParam {

    @Schema(description = "绩效计划名称", example = "2026 年第三季度绩效计划")
    private String search;

    @Schema(description = "是否已归档", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否已归档不能为空")
    private Boolean archived;

}
