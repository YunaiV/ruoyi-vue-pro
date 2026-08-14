package cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceConfirmationResultEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - HRM 员工端绩效运行阶段处理 Request VO")
@Data
public class HrmPortalPerformanceHandleStageReqVO {

    @Schema(description = "员工绩效考核编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工绩效考核不能为空")
    private Long assessmentId;

    @Schema(description = "运行阶段编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "运行阶段不能为空")
    private Long stageId;

    @Schema(description = "是否通过", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "处理结果不能为空")
    @InEnum(value = HrmPerformanceConfirmationResultEnum.class, message = "处理结果必须是 {value}")
    private Integer pass;

    @Schema(description = "处理意见", example = "审核通过")
    @Size(max = 500, message = "处理意见不能超过 500 个字符")
    private String comment;

    @Schema(description = "退回评分阶段编号列表", example = "[1024, 1025]")
    private List<Long> reviewStageIds;

}
