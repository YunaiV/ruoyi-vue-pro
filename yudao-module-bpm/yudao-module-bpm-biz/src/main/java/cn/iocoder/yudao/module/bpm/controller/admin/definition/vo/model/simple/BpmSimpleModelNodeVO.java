package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.*;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

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
    @InEnum(BpmSimpleModelNodeTypeEnum.class)
    private Integer type;

    @Schema(description = "模型节点名称", example = "领导审批")
    private String name;

    @Schema(description = "节点展示内容", example = "指定成员: 芋道源码")
    private String showText;

    @Schema(description = "子节点")
    private BpmSimpleModelNodeVO childNode; // 补充说明：在该模型下，子节点有且仅有一个，不会有多个

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

    @Schema(description = "是否需要签名", example = "false")
    private Boolean signEnable;

    @Schema(description = "是否填写审批意见", example = "false")
    private Boolean reasonRequire;

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

    /**
     * 创建任务监听器
     */
    private ListenerHandler taskCreateListener;
    /**
     * 指派任务监听器
     */
    private ListenerHandler taskAssignListener;
    /**
     * 完成任务监听器
     */
    private ListenerHandler taskCompleteListener;

    @Schema(description = "延迟器设置", example = "{}")
    private DelaySetting delaySetting;

    @Schema(description = "条件节点")
    private List<BpmSimpleModelNodeVO> conditionNodes; // 补充说明：有且仅有条件、并行、包容分支会使用

    /**
     * 条件节点设置
     */
    private ConditionSetting conditionSetting; // 仅用于条件节点 BpmSimpleModelNodeType.CONDITION_NODE

    @Schema(description = "路由分支组", example = "[]")
    private List<RouterSetting> routerGroups;

    @Schema(description = "路由分支默认分支 ID", example = "Flow_xxx", hidden = true) // 由后端生成，所以 hidden = true
    private String routerDefaultFlowId; // 仅用于路由分支节点 BpmSimpleModelNodeType.ROUTER_BRANCH_NODE

    /**
     * 触发器节点设置
     */
    private TriggerSetting triggerSetting;

    @Schema(description = "任务监听器")
    @Valid
    @Data
    public static class ListenerHandler {

        @Schema(description = "是否开启任务监听器", example = "false")
        @NotNull(message = "是否开启任务监听器不能为空")
        private Boolean enable;

        @Schema(description = "请求路径", example = "http://xxxxx")
        private String path;

        @Schema(description = "请求头", example = "[]")
        private List<HttpRequestParam> header;

        @Schema(description = "请求体", example = "[]")
        private List<HttpRequestParam> body;

    }

    @Schema(description = "HTTP 请求参数设置")
    @Data
    public static class HttpRequestParam {

        @Schema(description = "值类型", example = "1")
        @InEnum(BpmHttpRequestParamTypeEnum.class)
        @NotNull(message = "值类型不能为空")
        private Integer type;

        @Schema(description = "键", example = "xxx")
        @NotEmpty(message = "键不能为空")
        private String key;

        @Schema(description = "值", example = "xxx")
        @NotEmpty(message = "值不能为空")
        private String value;
    }

    @Schema(description = "审批节点拒绝处理策略")
    @Data
    public static class RejectHandler {

        @Schema(description = "拒绝处理类型", example = "1")
        @InEnum(BpmUserTaskRejectHandlerTypeEnum.class)
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

    @Schema(description = "条件设置")
    @Data
    @Valid
    // 仅用于条件节点 BpmSimpleModelNodeType.CONDITION_NODE
    public static class ConditionSetting {

        @Schema(description = "条件类型", example = "1")
        @InEnum(BpmSimpleModeConditionTypeEnum.class)
        private Integer conditionType;

        @Schema(description = "条件表达式", example = "${day>3}")
        private String conditionExpression;

        @Schema(description = "是否默认条件", example = "true")
        private Boolean defaultFlow;

        /**
         * 条件组
         */
        private ConditionGroups conditionGroups;
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

    @Schema(description = "延迟器")
    @Data
    @Valid
    public static class DelaySetting {

        @Schema(description = "延迟时间类型", example = "1")
        @NotNull(message = "延迟时间类型不能为空")
        @InEnum(BpmDelayTimerTypeEnum.class)
        private Integer delayType;

        @Schema(description = "延迟时间表达式", example = "PT1H,2025-01-01T00:00:00")
        @NotEmpty(message = "延迟时间表达式不能为空")
        private String delayTime;
    }

    @Schema(description = "路由分支")
    @Data
    @Valid
    public static class RouterSetting {

        @Schema(description = "节点 Id", example = "Activity_xxx") // 跳转到该节点
        @NotEmpty(message = "节点 Id 不能为空")
        private String nodeId;

        @Schema(description = "条件类型", example = "1")
        @InEnum(BpmSimpleModeConditionTypeEnum.class)
        @NotNull(message = "条件类型不能为空")
        private Integer conditionType;

        @Schema(description = "条件表达式", example = "${day>3}")
        private String conditionExpression;

        @Schema(description = "条件组", example = "{}")
        private ConditionGroups conditionGroups;
    }

    @Schema(description = "触发器节点配置")
    @Data
    @Valid
    public static class TriggerSetting {

        @Schema(description = "触发器类型", example = "1")
        @InEnum(BpmTriggerTypeEnum.class)
        @NotNull(message = "触发器类型不能为空")
        private Integer type;

        /**
         * http 请求触发器设置
         */
        @Valid
        private HttpRequestTriggerSetting httpRequestSetting;

        @Schema(description = "http 请求触发器设置", example = "{}")
        @Data
        public static class HttpRequestTriggerSetting {

            @Schema(description = "请求路径", example = "http://127.0.0.1")
            @NotEmpty(message = "请求 URL 不能为空")
            @URL(message = "请求 URL 格式不正确")
            private String url;

            @Schema(description = "请求头参数设置", example = "[]")
            @Valid
            private List<HttpRequestParam> header;

            @Schema(description = "请求头参数设置", example = "[]")
            @Valid
            private List<HttpRequestParam> body;
        }

    }
}
