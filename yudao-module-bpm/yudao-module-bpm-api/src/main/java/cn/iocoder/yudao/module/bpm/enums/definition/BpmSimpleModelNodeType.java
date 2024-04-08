package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
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
public enum BpmSimpleModelNodeType implements IntArrayValuable {

    // TODO @jaosn：-1、0、1、4、-2 是前端已经定义好的么？感觉未来可以考虑搞成和 BPMN 尽量一致的单词哈；类似 usertask 用户审批；
    // TODO @jason：_NODE 都删除掉哈；
    START_EVENT_NODE(0, "开始节点"),
    END_EVENT_NODE(-2, "结束节点"), // TODO @jaosn：挪到 START_EVENT_NODE 后；

    APPROVE_USER_NODE(1, "审批人节点"), // TODO @jaosn：是不是这里从 10 开始好点；相当于说，0-9 给开始和结束；10-19 给各种节点；20-29 给各种条件；TODO @jason：改成 USER_TASK 是不是好点呀
    // 抄送人节点、对应 BPMN 的 ScriptTask. 使用ScriptTask 原因。好像 ServiceTask 自定义属性不能写入 XML；
    // TODO @jason：ServiceTask 自定义 xml，有没啥报错信息；
    SCRIPT_TASK_NODE(2, "抄送人节点"), // TODO @jason：是不是改成 COPY_TASK 好一点哈；

    EXCLUSIVE_GATEWAY_NODE(4, "排他网关"), // TODO @jason：是不是改成叫 条件分支？
    PARALLEL_GATEWAY_FORK_NODE(5, "并行网关分叉节点"), // TODO @jason：是不是一个 并行分支 ？就可以啦？
    PARALLEL_GATEWAY_JOIN_NODE(6, "并行网关聚合节点"),
    INCLUSIVE_GATEWAY_FORK_NODE(7, "包容网关分叉节点"),
    INCLUSIVE_GATEWAY_JOIN_NODE(8, "包容网关聚合节点"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmSimpleModelNodeType::getType).toArray();

    private final Integer type;
    private final String name;

    public static boolean isGatewayNode(Integer type) {
        return Objects.equals(EXCLUSIVE_GATEWAY_NODE.getType(), type) || Objects.equals(PARALLEL_GATEWAY_FORK_NODE.getType(), type)
                || Objects.equals(INCLUSIVE_GATEWAY_FORK_NODE.getType(), type) ;
    }

    public static BpmSimpleModelNodeType valueOf(Integer type) {
        return ArrayUtil.firstMatch(nodeType -> nodeType.getType().equals(type), values());
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
