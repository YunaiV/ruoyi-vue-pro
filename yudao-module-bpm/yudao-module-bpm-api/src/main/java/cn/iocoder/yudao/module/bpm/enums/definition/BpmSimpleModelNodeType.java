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
    START_EVENT(0, "开始节点"),
    END_EVENT(-2, "结束节点"), // TODO @jaosn：挪到 START_EVENT_NODE 后；

    USER_TASK(1, "审批人节点"), // TODO @jaosn：是不是这里从 10 开始好点；相当于说，0-9 给开始和结束；10-19 给各种节点；20-29 给各种条件； TODO 后面改改
    COPY_TASK(2, "抄送人节点"),

    EXCLUSIVE_GATEWAY(4, "排他网关"), // TODO @jason：是不是改成叫 条件分支？
    PARALLEL_GATEWAY_FORK(5, "并行网关分叉节点"), // TODO @jason：是不是一个 并行分支 ？就可以啦？ 后面是否去掉并行网关。只用包容网关
    PARALLEL_GATEWAY_JOIN(6, "并行网关聚合节点"),
    INCLUSIVE_GATEWAY_FORK(7, "包容网关分叉节点"),
    INCLUSIVE_GATEWAY_JOIN(8, "包容网关聚合节点"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmSimpleModelNodeType::getType).toArray();

    private final Integer type;
    private final String name;

    /**
     * 判断是否为分支节点
     * @param type 节点类型
     */
    public static boolean isBranchNode(Integer type) {
        return Objects.equals(EXCLUSIVE_GATEWAY.getType(), type) || Objects.equals(PARALLEL_GATEWAY_FORK.getType(), type)
                || Objects.equals(INCLUSIVE_GATEWAY_FORK.getType(), type) ;
    }

    public static BpmSimpleModelNodeType valueOf(Integer type) {
        return ArrayUtil.firstMatch(nodeType -> nodeType.getType().equals(type), values());
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
