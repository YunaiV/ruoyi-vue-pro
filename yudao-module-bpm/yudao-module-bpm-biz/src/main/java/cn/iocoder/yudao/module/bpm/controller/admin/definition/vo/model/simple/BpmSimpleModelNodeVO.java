package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.*;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

    @Schema(description = "节点展示内容", example = "指定成员: 芋道源码")
    private String showText;

    @Schema(description = "子节点")
    private BpmSimpleModelNodeVO childNode; // 补充说明：在该模型下，子节点有且仅有一个，不会有多个

    @Schema(description = "条件节点")
    private List<BpmSimpleModelNodeVO> conditionNodes; // 补充说明：有且仅有条件、并行、包容等分支会使用

    @Schema(description = "条件类型", example = "1")
    @InEnum(BpmSimpleModeConditionType.class)
    private Integer conditionType; // 仅用于条件节点 BpmSimpleModelNodeType.CONDITION_NODE

    @Schema(description = "条件表达式", example = "${day>3}")
    private String conditionExpression; // 仅用于条件节点 BpmSimpleModelNodeType.CONDITION_NODE

    @Schema(description = "是否默认条件", example = "true")
    private Boolean defaultFlow; // 仅用于条件节点 BpmSimpleModelNodeType.CONDITION_NODE
    /**
     * 条件组
     */
    private ConditionGroups conditionGroups; // 仅用于条件节点 BpmSimpleModelNodeType.CONDITION_NODE

    @Schema(description = "候选人策略", example = "30")
    @InEnum(BpmTaskCandidateStrategyEnum.class)
    private Integer candidateStrategy; // 用于审批，抄送节点

    @Schema(description = "候选人参数")
    private String candidateParam; // 用于审批，抄送节点

    @Schema(description = "审批节点类型", example = "1")
    @InEnum(BpmUserTaskApproveTypeEnum.class)
    private Integer approveType; // 用于审批节点

    @Schema(description = "多人审批方式", example = "1")
    @InEnum(BpmUserTaskApproveMethodEnum.class)
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

    @Schema(description = "审批节点拒绝处理策略")
    @Data
    public static class RejectHandler {

        @Schema(description = "拒绝处理类型", example = "1")
        @InEnum(BpmUserTaskRejectHandlerType.class)
        private Integer type;

        @Schema(description = "任务拒绝后驳回的节点 Id", example = "Activity_1")
        private String returnNodeId;
    }

    @Schema(description = "审批节点超时处理策略")
    @Valid
    @Data
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

    @Schema(description = "空处理策略")
    @Data
    @Valid
    public static class AssignEmptyHandler {

        @Schema(description = "空处理类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "空处理类型不能为空")
        @InEnum(BpmUserTaskAssignEmptyHandlerTypeEnum.class)
        private Integer type;

        @Schema(description = "指定人员审批的用户编号数组", example = "1")
        private List<Long> userIds;

    }

    @Schema(description = "操作按钮设置")
    @Data
    @Valid
    public static class OperationButtonSetting {

        // TODO @jason：是不是按钮的标识？id 会和数据库的 id 自增有点模糊，key 标识会更合理一点点哈。
        @Schema(description = "按钮 Id", example = "1")
        private Integer id;

        @Schema(description = "显示名称", example = "审批")
        private String displayName;

        @Schema(description = "是否启用", example = "true")
        private Boolean enable;
    }

    @Schema(description = "条件组")
    @Data
    @Valid
    public static class ConditionGroups {

        @Schema(description = "条件组下的条件关系是否为与关系", example = "true")
        @NotNull(message = "条件关系不能为空")
        private Boolean and;

        @Schema(description = "条件组下的条件", example = "[]")
        @NotEmpty(message = "条件不能为空")
        private List<Condition> conditions;
    }

    @Schema(description = "条件")
    @Data
    @Valid
    public static class Condition {

        @Schema(description = "条件下的规则关系是否为与关系", example = "true")
        @NotNull(message = "规则关系不能为空")
        private Boolean and;

        @Schema(description = "条件下的规则", example = "[]")
        @NotEmpty(message = "规则不能为空")
        private List<ConditionRule> rules;
    }

    @Schema(description = "条件规则")
    @Data
    @Valid
    public static class ConditionRule {

        @Schema(description = "运行符号", example = "==")
        @NotEmpty(message = "运行符号不能为空")
        private String opCode;

        @Schema(description = "运算符左边的值,例如某个流程变量", example = "startUserId")
        @NotEmpty(message = "运算符左边的值不能为空")
        private String leftSide;

        @Schema(description = "运算符右边的值", example = "1")
        @NotEmpty(message = "运算符右边的值不能为空")
        private String rightSide;
    }

    // TODO @芋艿：条件；建议可以固化的一些选项；然后有个表达式兜底；要支持
}
