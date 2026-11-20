package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/**
 * 管理后台 - 退回修改后重新提交流程任务的 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 退回修改后重新提交流程任务的 Request VO")
@Data
public class BpmTaskResubmitReqVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "任务编号不能为空")
    private String taskId;

    @Schema(description = "退回修改的批次编号，来自审批详情", requiredMode = Schema.RequiredMode.REQUIRED, example = "a1b2c3")
    @NotEmpty(message = "退回修改的批次编号不能为空")
    private String revisionId;

    @Schema(description = "修改的表单变量，只能包含退回目标节点可编辑的字段", example = "{\"day\": 3}")
    private Map<String, Object> variables;

}
