package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script;

import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskRuleScriptEnum;
import org.flowable.engine.delegate.DelegateExecution;

import java.util.Set;

/**
 * Bpm 任务分配的自定义 Script 脚本
 * 使用场景：
 * 1. 设置审批人为发起人
 * 2. 设置审批人为发起人的 Leader
 * 3. 甚至审批人为发起人的 Leader 的 Leader
 *
 * @author 芋道源码
 */
public interface BpmTaskAssignScript {

    /**
     * 基于执行任务，获得任务的候选用户们
     *
     * @param execution 执行任务
     * @return 候选人用户的编号数组
     */
    Set<Long> calculateTaskCandidateUsers(DelegateExecution execution);

    /**
     * 获得枚举值
     *
     * @return 枚举值
     */
    BpmTaskRuleScriptEnum getEnum();
}

