package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.bpm.service.definition.BpmTaskAssignRuleService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.util.TaskHelper;
import org.flowable.task.service.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自定义的流程任务的 assignee 负责人的分配
 * 第一步，获得对应的分配规则；
 * 第二步，根据分配规则，计算出分配任务的候选人。如果找不到，则直接报业务异常，不继续执行后续的流程；
 * 第三步，随机选择一个候选人，则选择作为 assignee 负责人。
 *
 * @author 芋道源码
 */
@Slf4j
public class BpmUserTaskActivityBehavior extends UserTaskActivityBehavior {

    @Setter
    private BpmTaskAssignRuleService bpmTaskRuleService;

    public BpmUserTaskActivityBehavior(UserTask userTask) {
        super(userTask);
    }

    @Override
    @DataPermission(enable = false) // 不需要处理数据权限， 不然会有问题，查询不到数据
    protected void handleAssignments(TaskService taskService, String assignee, String owner,
        List<String> candidateUsers, List<String> candidateGroups, TaskEntity task, ExpressionManager expressionManager,
        DelegateExecution execution, ProcessEngineConfigurationImpl processEngineConfiguration) {
        // 第一步，获得任务的候选用户们
        Set<Long> candidateUserIds = bpmTaskRuleService.calculateTaskCandidateUsers(task);
        // 第二步，选择一个作为候选人
        Long assigneeUserId = chooseTaskAssignee(execution, candidateUserIds);
        // 第三步，设置作为负责人
        TaskHelper.changeTaskAssignee(task, String.valueOf(assigneeUserId));
    }

    private Long chooseTaskAssignee(DelegateExecution execution, Set<Long> candidateUserIds) {
        // 获取任务变量
        Map<String, Object> variables = execution.getVariables();
        // 设置任务集合变量key
        String expressionText = String.format("%s_userList", execution.getCurrentActivityId());
        // 判断当前任务是否为并行任务, 是的话获取任务变量
        if (variables.containsKey(expressionText)) {
            String user = variables.get("user").toString();
            return Long.valueOf(user);
        }

        // TODO 芋艿：未来可以优化下，改成轮询的策略
        int index = RandomUtil.randomInt(candidateUserIds.size());
        return CollUtil.get(candidateUserIds, index);
    }

}
