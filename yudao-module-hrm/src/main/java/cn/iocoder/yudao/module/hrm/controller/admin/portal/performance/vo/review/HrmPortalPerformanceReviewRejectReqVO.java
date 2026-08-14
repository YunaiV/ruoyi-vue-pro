package cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - HRM 员工端绩效评分阶段驳回 Request VO")
@Data
public class HrmPortalPerformanceReviewRejectReqVO {

    @Schema(description = "员工绩效考核编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工绩效考核不能为空")
    private Long assessmentId;

    @Schema(description = "评分阶段编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "当前评分阶段不能为空")
    private Long reviewStageId;

    @Schema(description = "绩效评审驳回原因", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "评分依据不完整，请补充说明")
    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 1000, message = "驳回原因不能超过 1000 个字符")
    private String reason;

}
