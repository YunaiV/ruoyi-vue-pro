package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Schema(description = "管理后台 - 表单字段权限 Request VO")
@Data
public class BpmFormFieldsPermissionReqVO {

    @Schema(description = "流程定义的编号", example = "1024")
    private String processDefinitionId;

    @Schema(description = "流程实例的编号", example = "1024")
    private String processInstanceId;

    @Schema(description = "流程活动编号",  example = "StartUserNode")
    private String activityId; // 对应 BPMN XML 节点 Id

    @Schema(description = "流程任务编号", example = "95f2f08b-621b-11ef-bf39-00ff4722db8b")
    private String taskId; // UserTask 对应的Id

    @AssertTrue(message = "流程定义的编号和流程实例的编号不能同时为空")
    @JsonIgnore
    public boolean isValidProcessParam() {
        return StrUtil.isNotEmpty(processDefinitionId) || StrUtil.isNotEmpty(processInstanceId);
    }

    @AssertTrue(message = "流程活动编号和流程任务编号编号不能同时为空")
    @JsonIgnore
    public boolean isValidActivityParam() {
        return StrUtil.isNotEmpty(activityId) || StrUtil.isNotEmpty(taskId);
    }

}
