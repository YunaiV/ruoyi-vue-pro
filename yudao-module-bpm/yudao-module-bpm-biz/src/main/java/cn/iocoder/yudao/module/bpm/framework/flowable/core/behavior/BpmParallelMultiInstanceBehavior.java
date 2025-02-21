package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import lombok.Setter;
import org.flowable.bpmn.model.Activity;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;

import java.util.Set;

/**
 * 自定义的【并行】的【多个】流程任务的 assignee 负责人的分配
 * 第一步，基于分配规则，计算出分配任务的【多个】候选人们。
 * 第二步，将【多个】任务候选人们，设置到 DelegateExecution 的 collectionVariable 变量中，以便 BpmUserTaskActivityBehavior 使用它
 *
 * @author kemengkai
 * @since 2022-04-21 16:57
 */
@Setter
public class BpmParallelMultiInstanceBehavior extends ParallelMultiInstanceBehavior {

    private BpmTaskCandidateInvoker taskCandidateInvoker;

    public BpmParallelMultiInstanceBehavior(Activity activity,
                                            AbstractBpmnActivityBehavior innerActivityBehavior) {
        super(activity, innerActivityBehavior);
    }

    /**
     * 重写该方法，主要实现两个功能：
     * 1. 忽略原有的 collectionVariable、collectionElementVariable 表达式，而是采用自己定义的
     * 2. 获得任务的处理人，并设置到 collectionVariable 中，用于 BpmUserTaskActivityBehavior 从中可以获取任务的处理人
     *
     * 注意，多个任务实例，每个任务实例对应一个处理人，所以返回的数量就是任务处理人的数量
     *
     * @param execution 执行任务
     * @return 数量
     */
    @Override
    protected int resolveNrOfInstances(DelegateExecution execution) {
        // 第一步，设置 collectionVariable 和 CollectionVariable
        // 从  execution.getVariable() 读取所有任务处理人的 key
        super.collectionExpression = null; // collectionExpression 和 collectionVariable 是互斥的
        super.collectionVariable = FlowableUtils.formatExecutionCollectionVariable(execution.getCurrentActivityId());
        // 从 execution.getVariable() 读取当前所有任务处理的人的 key
        super.collectionElementVariable = FlowableUtils.formatExecutionCollectionElementVariable(execution.getCurrentActivityId());

        // 第二步，获取任务的所有处理人
        @SuppressWarnings("unchecked")
        Set<Long> assigneeUserIds = (Set<Long>) execution.getVariable(super.collectionVariable, Set.class);
        if (assigneeUserIds == null) {
            assigneeUserIds = taskCandidateInvoker.calculateUsersByTask(execution);
            if (CollUtil.isEmpty(assigneeUserIds)) {
                // 特殊：如果没有处理人的情况下，至少有一个 null 空元素，避免自动通过！
                // 这样，保证在 BpmUserTaskActivityBehavior 至少创建出一个 Task 任务
                // 用途：1）审批人为空时；2）审批类型为自动通过、自动拒绝时
                assigneeUserIds = SetUtils.asSet((Long) null);
            }
            execution.setVariableLocal(super.collectionVariable, assigneeUserIds);
        }
        return assigneeUserIds.size();
    }

}
