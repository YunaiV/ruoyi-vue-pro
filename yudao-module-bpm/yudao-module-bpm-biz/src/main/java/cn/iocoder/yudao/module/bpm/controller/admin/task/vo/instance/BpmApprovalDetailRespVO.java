package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import cn.iocoder.yudao.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Schema(description = "管理后台 - 审批详情 Response VO")
@Data
public class BpmApprovalDetailRespVO {

    @Schema(description = "流程实例的状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status; // 参见 BpmProcessInstanceStatusEnum 枚举

    @Schema(description = "活动节点列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ActivityNode> activityNodes;

    @Schema(description = "表单字段权限")
    private Map<String, String> formFieldsPermission;

    @Schema(description = "待办任务")
    private BpmTaskRespVO todoTask;

    /**
     * 所属流程定义信息
     */
    private BpmProcessDefinitionRespVO processDefinition;

    /**
     * 所属流程实例信息
     */
    private BpmProcessInstanceRespVO processInstance;

    @Schema(description = "活动节点信息")
    @Data
    public static class ActivityNode {

        @Schema(description = "节点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "StartUserNode")
        private String id;

        @Schema(description = "节点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "发起人")
        private String name;

        @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer nodeType; // 参见 BpmSimpleModelNodeType 枚举

        @Schema(description = "节点状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
        private Integer status; // 参见 BpmTaskStatusEnum 枚举

        @Schema(description = "节点的开始时间")
        private LocalDateTime startTime;
        @Schema(description = "节点的结束时间")
        private LocalDateTime endTime;

        @Schema(description = "审批节点的任务信息")
        private List<ActivityNodeTask> tasks;

        @Schema(description = "候选人策略", example = "35")
        private Integer candidateStrategy; // 参见 BpmTaskCandidateStrategyEnum 枚举。主要用于发起时，审批节点、抄送节点自选

        @Schema(description = "候选人用户 ID 列表", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1818")
        @JsonIgnore // 不返回，只是方便后续读取，赋值给 candidateUsers
        private List<Long> candidateUserIds;

        @Schema(description = "候选人用户列表")
        private List<UserSimpleBaseVO> candidateUsers; // 只包含未生成 ApprovalTaskInfo 的用户列表

    }

    @Schema(description = "活动节点的任务信息")
    @Data
    public static class ActivityNodeTask {

        @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private String id;

        @Schema(description = "任务所属人编号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1818")
        @JsonIgnore // 不返回，只是方便后续读取，赋值给 ownerUser
        private Long owner;

        @Schema(description = "任务所属人", example = "1024")
        private UserSimpleBaseVO ownerUser;

        @Schema(description = "任务分配人编号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2048")
        @JsonIgnore // 不返回，只是方便后续读取，赋值给 assigneeUser
        private Long assignee;

        @Schema(description = "任务分配人", example = "2048")
        private UserSimpleBaseVO assigneeUser;

        @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer status;  // 参见 BpmTaskStatusEnum 枚举

        @Schema(description = "审批意见", example = "同意")
        private String reason;

    }

}
