package cn.iocoder.yudao.module.hrm.controller.admin.portal.performance.vo.process;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hrm.enums.performance.assessment.HrmPerformanceConfirmationResultEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - HRM 员工端绩效确认 Request VO")
@Data
public class HrmPortalPerformanceConfirmReqVO {

    @Schema(description = "员工绩效考核编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "员工绩效考核不能为空")
    private Long assessmentId;

    @Schema(description = "是否通过", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "确认结果不能为空")
    @InEnum(value = HrmPerformanceConfirmationResultEnum.class, message = "确认结果必须是 {value}")
    private Integer pass;

    @Schema(description = "说明", example = "确认考核目标")
    @Size(max = 1000, message = "说明不能超过 1000 个字符")
    private String comment;

}
