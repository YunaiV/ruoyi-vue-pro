package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior;

import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import lombok.Setter;
import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.flowable.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.flowable.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;

/**
 * 自定义的 ActivityBehaviorFactory 实现类，目的如下：
 * 1. 自定义 {@link #createUserTaskActivityBehavior(UserTask)}：实现自定义的流程任务的 assignee 负责人的分配
 *
 * @author 芋道源码
 */
@Setter
public class BpmActivityBehaviorFactory extends DefaultActivityBehaviorFactory {

    private BpmTaskCandidateInvoker taskCandidateInvoker;

    @Override
    public UserTaskActivityBehavior createUserTaskActivityBehavior(UserTask userTask) {
        return new BpmUserTaskActivityBehavior(userTask)
                .setTaskCandidateInvoker(taskCandidateInvoker);
    }

    @Override
    public ParallelMultiInstanceBehavior createParallelMultiInstanceBehavior(Activity activity,
                                                                             AbstractBpmnActivityBehavior behavior) {
        return new BpmParallelMultiInstanceBehavior(activity, behavior)
                .setTaskCandidateInvoker(taskCandidateInvoker);
    }

    @Override
    public SequentialMultiInstanceBehavior createSequentialMultiInstanceBehavior(Activity activity,
                                                                                 AbstractBpmnActivityBehavior behavior) {
        return new BpmSequentialMultiInstanceBehavior(activity, behavior)
                .setTaskCandidateInvoker(taskCandidateInvoker);
    }

}
