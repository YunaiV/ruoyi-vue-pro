package cn.iocoder.yudao.module.bpm.enums.task;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 流程任务 Task 的状态枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmTaskStatusEnum implements ArrayValuable<Integer> {

    /**
     * 使用场景：
     * 1. 节点因为跳过表达式等原因未执行，审批详情中展示为跳过（不会记录到任务上）
     * 2. 审批超时【自动跳转】到其它节点时，当前节点未处理的任务，会被标记为跳过
     */
    SKIP(-2, "跳过"),
    /**
     * 使用场景：
     * 1. 审批详情中，预测的未来节点尚未执行，展示为未开始（不会记录到任务上）
     */
    NOT_START(-1, "未开始"),
    /**
     * 使用场景：
     * 1. 任务创建后，等待审批人处理
     */
    RUNNING(1, "审批中"),
    /**
     * 使用场景：
     * 1. 审批人审批通过，或者系统自动通过（例如说，审批超时自动同意）
     */
    APPROVE(2, "审批通过"),
    /**
     * 使用场景：
     * 1. 审批人审批不通过，或者系统自动不通过（例如说，审批超时自动拒绝）
     */
    REJECT(3, "审批不通过"),
    /**
     * 使用场景：
     * 1. 流程实例被取消，或者多人审批已经满足条件等，无需审批的任务被系统取消
     */
    CANCEL(4, "已取消"),

    /**
     * 使用场景：
     * 1. 任务被退回到之前的节点，当前节点的任务结束
     */
    RETURN(5, "已退回"),

    /**
     * 使用场景：
     * 1. 节点的退回后审批方式为【从当前节点审批】时，任务退回后不跳转，变成 WAIT_REVISION 这个状态，
     *    等到退回节点的处理人修改表单、重新提交后，再变回 RUNNING 审批中，由原审批人继续审批
     */
    WAIT_REVISION(8, "已退回，等待修改"),

    /**
     * 使用场景：
     * 1. 任务被向后【加签】时，它在审批通过后，会变成 APPROVING 这个状态，然后等到【加签】出来的任务都被审批后，才会变成 APPROVE 审批通过
     */
    APPROVING(7, "审批通过中"),
    /**
     * 使用场景：
     * 1. 任务被向前【加签】时，它会变成 WAIT 状态，需要等待【加签】出来的任务被审批后，它才能继续变为 RUNNING 继续审批
     * 2. 任务被向后【加签】时，【加签】出来的任务处于 WAIT 状态，它们需要等待该任务被审批后，它们才能继续变为 RUNNING 继续审批
     */
    WAIT(0, "待审批");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmTaskStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     * <p>
     * 如果新增时，注意 {@link #isEndStatus(Integer)} 是否需要变更
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isRejectStatus(Integer status) {
        return REJECT.getStatus().equals(status);
    }

    /**
     * 判断该状态是否已经处于 End 最终状态
     * <p>
     * 主要用于一些状态更新的逻辑，如果已经是最终状态，就不再进行更新
     *
     * @param status 状态
     * @return 是否
     */
    public static boolean isEndStatus(Integer status) {
        return ObjectUtils.equalsAny(status,
                APPROVE.getStatus(), REJECT.getStatus(), CANCEL.getStatus(),
                RETURN.getStatus(), APPROVING.getStatus(), SKIP.getStatus());
    }

    public static boolean isCancelStatus(Integer status) {
        return ObjUtil.equal(status, CANCEL.getStatus());
    }

    public static BpmTaskStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
