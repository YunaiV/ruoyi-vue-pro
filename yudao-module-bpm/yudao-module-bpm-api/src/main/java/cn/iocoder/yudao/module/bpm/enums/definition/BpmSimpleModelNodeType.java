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
    // @芋艿 感觉还是用 START_NODE . END_NODE 比较好.
    // 0 1 开始和结束
    START_NODE(0, "开始节点"),
    END_NODE(1, "结束节点"), // TODO @jaosn：挪到 START_EVENT_NODE 后；

    // 10 ~ 49 各种节点
    START_USER_NODE(10, "发起人节点"), // 发起人节点。前端的开始节点，Id 固定
    APPROVE_NODE(11, "审批人节点"), // TODO @jaosn：是不是这里从 10 开始好点；相当于说，0-9 给开始和结束；10-19 给各种节点；20-29 给各种条件； TODO 后面改改
    COPY_NODE(12, "抄送人节点"),

    // 50 ~ 条件分支
    CONDITION_NODE(50, "条件节点"), // 用于构建流转条件的表达式
    CONDITION_BRANCH_NODE(51, "条件分支节点"), // TODO @jason：是不是改成叫 条件分支？
    PARALLEL_BRANCH_NODE(52, "并行分支节点"), // TODO @jason：是不是一个 并行分支 ？就可以啦？ 后面是否去掉并行网关。只用包容网关
    INCLUSIVE_BRANCH_NODE(53, "包容分叉节点"),
    // TODO @jason：建议整合 join，最终只有 条件分支、并行分支、包容分支，三种~
    // TODO @芋艿。 感觉还是分开好理解一点,也好处理一点。前端结构中把聚合节点显示并传过来。
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmSimpleModelNodeType::getType).toArray();

    private final Integer type;
    private final String name;

    /**
     * 判断是否为分支节点
     *
     * @param type 节点类型
     */
    public static boolean isBranchNode(Integer type) {
        return Objects.equals(CONDITION_BRANCH_NODE.getType(), type)
                || Objects.equals(PARALLEL_BRANCH_NODE.getType(), type)
                || Objects.equals(INCLUSIVE_BRANCH_NODE.getType(), type) ;
    }

    public static BpmSimpleModelNodeType valueOf(Integer type) {
        return ArrayUtil.firstMatch(nodeType -> nodeType.getType().equals(type), values());
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
