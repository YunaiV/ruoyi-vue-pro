package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Schema(description = "管理后台 - 审批详情 Request VO")
@Data
public class BpmApprovalDetailReqVO {

    // TODO @jason：这里要不注释说明下，什么情况下，使用 processDefinitionId、processInstanceId、activityId、taskId。

    @Schema(description = "流程定义的编号", example = "1024")
    private String processDefinitionId; // 发起流程的时候传流程定义 Id

    @Schema(description = "流程实例的编号", example = "1024")
    private String processInstanceId;  // 流程已发起时候传流程实例 Id

    @Schema(description = "流程活动编号",  example = "StartUserNode")
    private String activityId; // 用于获取表单权限。 发起流程时, 传‘发起人节点’ activityId 可获取发起人的表单权限

    @Schema(description = "流程任务编号", example = "95f2f08b-621b-11ef-bf39-00ff4722db8b")
    private String taskId; // 用于获取表单权限。流程已发起时，传任务 Id, 获取任务节点的变得权限

    @AssertTrue(message = "流程定义的编号和流程实例的编号不能同时为空")
    @JsonIgnore
    public boolean isValidProcessParam() {
        return StrUtil.isNotEmpty(processDefinitionId) || StrUtil.isNotEmpty(processInstanceId);
    }

}
