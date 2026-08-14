package cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.review;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工端绩效评分 Request VO")
@Data
public class HrmPortalPerformanceScoreReqVO {

    @Schema(description = "员工绩效考核编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工绩效考核不能为空")
    private Long assessmentId;

    @Schema(description = "评分阶段编号", example = "2048")
    private Long reviewStageId;

    @Schema(description = "说明", example = "本阶段评分完成")
    @Size(max = 2000, message = "说明不能超过 2000 个字符")
    private String comment;

    @Schema(description = "自评说明", example = "已完成本季度目标")
    @Size(max = 2000, message = "自评说明不能超过 2000 个字符")
    private String selfComment;

    @Schema(description = "评分人说明", example = "目标完成情况良好")
    @Size(max = 2000, message = "评分人说明不能超过 2000 个字符")
    private String reviewerComment;

    @Schema(description = "指标列表")
    @Valid
    private List<HrmPortalPerformanceQuotaSaveReqVO> quotas;

}
