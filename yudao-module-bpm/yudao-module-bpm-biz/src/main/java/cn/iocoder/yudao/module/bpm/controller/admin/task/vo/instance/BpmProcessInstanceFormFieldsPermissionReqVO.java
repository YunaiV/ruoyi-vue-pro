package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author jason
 */
@Schema(description = "管理后台 - 流程实例表单字段权限 Request VO")
@Data
public class BpmProcessInstanceFormFieldsPermissionReqVO {

    @Schema(description = "流程实例的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "流程实例的编号不能为空")
    private String id;

    @Schema(description = "流程活动编号",  example = "StartUserNode")
    private String activityId; // 对应 BPMN XML 节点 Id

    @Schema(description = "流程任务的编号", example = "95f2f08b-621b-11ef-bf39-00ff4722db8b")
    private String taskId; // UserTask 对应的Id
}
