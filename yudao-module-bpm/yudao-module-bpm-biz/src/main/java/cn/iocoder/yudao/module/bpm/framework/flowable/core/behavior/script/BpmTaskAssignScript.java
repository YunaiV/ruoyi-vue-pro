package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script;

import cn.iocoder.yudao.module.bpm.domain.enums.definition.BpmTaskRuleScriptEnum;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

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
     * 基于流程任务，获得任务的候选用户们
     *
     * @param task 任务
     * @return 候选人用户的编号数组
     */
    Set<Long> calculateTaskCandidateUsers(TaskEntity task);

    /**
     * 获得枚举值
     *
     * @return 枚举值
     */
    BpmTaskRuleScriptEnum getEnum();
}

