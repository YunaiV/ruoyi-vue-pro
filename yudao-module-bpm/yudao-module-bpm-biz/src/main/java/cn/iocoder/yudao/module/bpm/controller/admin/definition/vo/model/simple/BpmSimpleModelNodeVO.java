package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.*;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 仿钉钉流程设计模型节点 VO")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BpmSimpleModelNodeVO {

    @Schema(description = "模型节点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "StartEvent_1")
    @NotEmpty(message = "模型节点编号不能为空")
    private String id;

    @Schema(description = "模型节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模型节点类型不能为空")
    @InEnum(BpmSimpleModelNodeType.class)
    private Integer type;

    @Schema(description = "模型节点名称", example = "领导审批")
    private String name;

    // TODO @jason：要不改成 placeholder 和一般 Element-Plus 组件一致。占位符，用于展示。@芋艿。这个不是 placeholder 占位符的含义。节点配置后。节点展示的内容，不知道取什么名字好???
    // TODO @jason：【回复】占位文本（showText）是指当一个文本框没有被 focus 的时候显示的是提示文字，当他被点击之后就显示空白。。。虽然不是完全精准，但是 placeholder 相对正式点~
    @Schema(description = "节点展示内容", example = "指定成员: 芋道源码")
    private String showText;

    @Schema(description = "子节点")
    private BpmSimpleModelNodeVO childNode; // 补充说明：在该模型下，子节点有且仅有一个，不会有多个

    @Schema(description = "条件节点")
    private List<BpmSimpleModelNodeVO> conditionNodes; // 补充说明：有且仅有条件、并行、包容等分支会使用

    @Schema(description = "节点的属性")
    private Map<String, Object> attributes; // TODO @jason：建议是字段分拆下；类似说：

    // TODO @jason：看看是不是可以简化；@芋艿： 暂时先放着。不知道后面是否会用到
    /**
     * 附加节点 Id, 该节点不从前端传入。 由程序生成. 由于当个节点无法完成功能。 需要附加节点来完成。
     */
    @JsonIgnore
    private String attachNodeId;

    @Schema(description = "候选人策略", example = "30")
    @InEnum(BpmTaskCandidateStrategyEnum.class)
    private Integer candidateStrategy; // 用于审批，抄送节点

    @Schema(description = "候选人参数")
    private String candidateParam; // 用于审批，抄送节点

    @Schema(description = "多人审批方式", example = "1")
    @InEnum(BpmApproveMethodEnum.class)
    private Integer approveMethod; // 用于审批节点

    @Schema(description = "通过比例", example = "100")
    private Integer approveRatio; // 通过比例，当多人审批方式为：多人会签(按通过比例) 需要设置

    @Schema(description = "表单权限", example = "[]")
    private List<Map<String, String>> fieldsPermission;

    @Schema(description = "操作按钮设置", example = "[]")
    private List<OperationButtonSetting> buttonsSetting;  // 用于审批节点

    /**
     * 审批节点拒绝处理
     */
    private RejectHandler rejectHandler;

    /**
     * 审批节点超时处理
     */
    private TimeoutHandler timeoutHandler;

    @Schema(description = "审批节点的审批人与发起人相同时，对应的处理类型", example = "1")
    @InEnum(BpmUserTaskAssignStartUserHandlerTypeEnum.class)
    private Integer assignStartUserHandlerType;

    /**
     * 空处理策略
     */
    private AssignEmptyHandler assignEmptyHandler;

    @Data
    @Schema(description = "审批节点拒绝处理策略")
    public static class RejectHandler {

        @Schema(description = "拒绝处理类型", example = "1")
        @InEnum(BpmUserTaskRejectHandlerType.class)
        private Integer type;

        @Schema(description = "任务拒绝后驳回的节点 Id", example = "Activity_1")
        private String returnNodeId;
    }

    @Data
    @Schema(description = "审批节点超时处理策略")
    @Valid
    public static class TimeoutHandler {

        @Schema(description = "是否开启超时处理", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
        @NotNull(message = "是否开启超时处理不能为空")
        private Boolean enable;

        @Schema(description = "任务超时未处理的行为", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "任务超时未处理的行为不能为空")
        @InEnum(BpmUserTaskTimeoutHandlerTypeEnum.class)
        private Integer type;

        @Schema(description = "超时时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "PT6H")
        @NotEmpty(message = "超时时间不能为空")
        private String timeDuration;

        @Schema(description = "最大提醒次数", example = "1")
        private Integer maxRemindCount;

    }

    @Data
    @Schema(description = "空处理策略")
    @Valid
    public static class AssignEmptyHandler {

        @Schema(description = "空处理类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "空处理类型不能为空")
        @InEnum(BpmUserTaskAssignEmptyHandlerTypeEnum.class)
        private Integer type;

        @Schema(description = "指定人员审批的用户编号数组", example = "1")
        private List<Long> userIds;

    }

    @Data
    @Schema(description = "操作按钮设置")
    public static class OperationButtonSetting {

        @Schema(description = "按钮 Id", example = "1")
        private  Integer id;

        @Schema(description = "显示名称", example = "审批")
        private String displayName;

        @Schema(description = "是否启用", example = "true")
        private Boolean enable;
    }

    // Map<String, Integer> formPermissions; 表单权限；仅发起、审批、抄送节点会使用
    // TODO @芋艿：⑥ 没有人的策略？
    // TODO @芋艿：条件；建议可以固化的一些选项；然后有个表达式兜底；要支持

}
