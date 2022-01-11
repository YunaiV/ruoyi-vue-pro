package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmTaskRuleDO;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.definition.BpmTaskRuleTypeEnum;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmTaskRuleService;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import lombok.Setter;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 自定义的流程任务的 assignee 负责人的分配
 *
 * @author 芋道源码
 */
public class BpmUserTaskActivitiBehavior extends UserTaskActivityBehavior {

    @Setter
    private BpmTaskRuleService bpmTaskRuleService;

    public BpmUserTaskActivitiBehavior(UserTask userTask) {
        super(userTask);
    }

    @Override
    protected void handleAssignments(TaskEntityManager taskEntityManager,
                                     String assignee, String owner, List<String> candidateUsers, List<String> candidateGroups,
                                     TaskEntity task, ExpressionManager expressionManager, DelegateExecution execution) {
        // 获得任务的规则
        BpmTaskRuleDO rule = getTaskRule(task);
        // 获得任务的候选用户们
        Set<Long> candidateUserIds = calculateTaskCandidateUsers(task, rule);
        // 设置负责人
        Long assigneeUserId = chooseTaskAssignee(candidateUserIds);
        taskEntityManager.changeTaskAssignee(task, String.valueOf(assigneeUserId));
        // 设置候选人们
        candidateUserIds.remove(assigneeUserId); // 已经成为负责人了，就不要在扮演候选人了
        if (CollUtil.isNotEmpty(candidateUserIds)) {
            task.addCandidateUsers(CollectionUtils.convertSet(candidateUserIds, String::valueOf));
        }
    }

    private BpmTaskRuleDO getTaskRule(TaskEntity task) {
        List<BpmTaskRuleDO> taskRules = bpmTaskRuleService.getTaskRulesByProcessDefinitionId(task.getProcessDefinitionId(),
                task.getTaskDefinitionKey());
        if (CollUtil.isEmpty(taskRules)) {
            throw new ActivitiException(StrUtil.format("流程任务({}/{}/{}) 找不到符合的任务规则",
                    task.getId(), task.getProcessDefinitionId(), task.getTaskDefinitionKey()));
        }
        if (taskRules.size() > 1) {
            throw new ActivitiException(StrUtil.format("流程任务({}/{}/{}) 找到过多任务规则({})",
                    task.getId(), task.getProcessDefinitionId(), task.getTaskDefinitionKey(), taskRules.size()));
        }
        return taskRules.get(0);
    }

    private Long chooseTaskAssignee(Set<Long> candidateUserIds) {
        // TODO 芋艿：未来可以优化下，改成轮询的策略
        int index = RandomUtil.randomInt(candidateUserIds.size());
        return CollUtil.get(candidateUserIds, index);
    }

    private Set<Long> calculateTaskCandidateUsers(TaskEntity task, BpmTaskRuleDO rule) {
        Set<Long> assigneeUserIds = null;
        if (Objects.equals(BpmTaskRuleTypeEnum.ROLE.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByRole(task, rule);
        } else if (Objects.equals(BpmTaskRuleTypeEnum.DEPT.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByDept(task, rule);
        } else if (Objects.equals(BpmTaskRuleTypeEnum.DEPT_LEADER.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByDept(task, rule);
        } else if (Objects.equals(BpmTaskRuleTypeEnum.USER.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByUser(task, rule);
        } else if (Objects.equals(BpmTaskRuleTypeEnum.USER_GROUP.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByUser(task, rule);
        }

        // TODO 芋艿：统一过滤掉被禁用的用户
        // 如果候选人为空，抛出异常 TODO 芋艿：没候选人的策略选择。1 - 挂起；2 - 直接结束；3 - 强制一个兜底人
        if (CollUtil.isEmpty(assigneeUserIds)) {
            throw new ActivitiException(StrUtil.format("流程任务({}/{}/{}) 任务规则({}) 找不到候选人",
                    task.getId(), task.getProcessDefinitionId(), task.getTaskDefinitionKey()));
        }
        return assigneeUserIds;
    }

    private Set<Long> calculateTaskCandidateUsersByRole(TaskEntity task, BpmTaskRuleDO rule) {
        throw new UnsupportedOperationException("暂不支持该任务规则");
    }


    private Set<Long> calculateTaskCandidateUsersByDept(TaskEntity task, BpmTaskRuleDO rule) {
        throw new UnsupportedOperationException("暂不支持该任务规则");
    }

    private Set<Long> calculateTaskCandidateUsersByDeptLeader(TaskEntity task, BpmTaskRuleDO rule) {
        throw new UnsupportedOperationException("暂不支持该任务规则");
    }

    private Set<Long> calculateTaskCandidateUsersByUser(TaskEntity task, BpmTaskRuleDO rule) {
        return rule.getOptions();
    }

    private Set<Long> calculateTaskCandidateUsersByUserGroup(TaskEntity task, BpmTaskRuleDO rule) {
        throw new UnsupportedOperationException("暂不支持该任务规则");
    }

    private Set<Long> calculateTaskCandidateUsersByScript(TaskEntity task, BpmTaskRuleDO rule) {
        throw new UnsupportedOperationException("暂不支持该任务规则");
    }

}
