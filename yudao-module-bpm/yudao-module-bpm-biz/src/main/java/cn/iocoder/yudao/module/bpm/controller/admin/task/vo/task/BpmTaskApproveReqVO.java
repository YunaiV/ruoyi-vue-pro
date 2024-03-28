package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

@Schema(description = "管理后台 - 通过流程任务的 Request VO")
@Data
public class BpmTaskApproveReqVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @Schema(description = "审批意见", requiredMode = Schema.RequiredMode.REQUIRED, example = "不错不错！")
    @NotEmpty(message = "审批意见不能为空")
    private String reason;

    @Schema(description = "抄送的用户编号数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1,2")
    private Collection<Long> copyUserIds;

    @Schema(description = "变量实例（动态表单）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> variables;

}
