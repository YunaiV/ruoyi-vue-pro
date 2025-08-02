package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 仿钉钉的流程器设计器的模型节点类型
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmSimpleModelNodeTypeEnum implements ArrayValuable<Integer> {

    // 0 ~ 1 开始和结束
    START_NODE(0, "开始", "startEvent"),
    END_NODE(1, "结束", "endEvent"),

    // 10 ~ 49 各种节点
    START_USER_NODE(10, "发起人", "userTask"), // 发起人节点。前端的开始节点，Id 固定
    APPROVE_NODE(11, "审批人", "userTask"),
    COPY_NODE(12, "抄送人", "serviceTask"),
    TRANSACTOR_NODE(13, "办理人", "userTask"),

    DELAY_TIMER_NODE(14, "延迟器", "receiveTask"),
    TRIGGER_NODE(15, "触发器", "serviceTask"),

    CHILD_PROCESS(20, "子流程", "callActivity"),

    // 50 ~ 条件分支
    CONDITION_NODE(50, "条件", "sequenceFlow"), // 用于构建流转条件的表达式
    CONDITION_BRANCH_NODE(51, "条件分支", "exclusiveGateway"),
    PARALLEL_BRANCH_NODE(52, "并行分支", "inclusiveGateway"), // 并行分支使用包容网关实现，条件表达式结果设置为 true
    INCLUSIVE_BRANCH_NODE(53, "包容分支", "inclusiveGateway"),
    ROUTER_BRANCH_NODE(54, "路由分支", "exclusiveGateway")
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmSimpleModelNodeTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;
    private final String bpmnType;

    /**
     * 判断是否为分支节点
     *
     * @param type 节点类型
     */
    public static boolean isBranchNode(Integer type) {
        return Objects.equals(CONDITION_BRANCH_NODE.getType(), type)
                || Objects.equals(PARALLEL_BRANCH_NODE.getType(), type)
                || Objects.equals(INCLUSIVE_BRANCH_NODE.getType(), type)
                || Objects.equals(ROUTER_BRANCH_NODE.getType(), type);
    }

    public static BpmSimpleModelNodeTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(nodeType -> nodeType.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
