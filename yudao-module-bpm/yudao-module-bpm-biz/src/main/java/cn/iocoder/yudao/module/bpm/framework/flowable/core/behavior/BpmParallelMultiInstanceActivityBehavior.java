package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmTaskAssignRuleDO;
import cn.iocoder.yudao.module.bpm.domain.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.BpmTaskAssignScript;
import cn.iocoder.yudao.module.bpm.service.definition.BpmTaskAssignRuleService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmUserGroupService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.annotations.VisibleForTesting;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.Activity;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.util.CommandContextUtil;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.TASK_CREATE_FAIL_NO_CANDIDATE_USER;

/**
 * @author kemengkai
 * @create 2022-04-21 16:57
 */
@Slf4j
public class BpmParallelMultiInstanceActivityBehavior extends ParallelMultiInstanceBehavior {

    @Setter
    private BpmTaskAssignRuleService bpmTaskRuleService;
    @Setter
    private BpmUserGroupService userGroupService;
    @Setter
    private DeptApi deptApi;
    @Setter
    private AdminUserApi adminUserApi;
    @Setter
    private PermissionApi permissionApi;
    /**
     * EL表达式集合模板
     */
    private final static String EXPRESSION_TEXT_TEMPLATE = "${coll_userList}";

    /**
     * 任务分配脚本
     */
    private Map<Long, BpmTaskAssignScript> scriptMap = Collections.emptyMap();

    public BpmParallelMultiInstanceActivityBehavior(Activity activity,
        AbstractBpmnActivityBehavior innerActivityBehavior) {
        super(activity, innerActivityBehavior);
    }

    public void setScripts(List<BpmTaskAssignScript> scripts) {
        this.scriptMap = convertMap(scripts, script -> script.getEnum().getId());
    }

    /**
     * 创建并行任务
     *
     * @param multiInstanceRootExecution 并行任务入参
     *
     * @return 返回结果
     */
    @Override
    protected int createInstances(DelegateExecution multiInstanceRootExecution) {
        // 查找任务信息
        BpmTaskAssignRuleDO taskRule = getTaskRule(multiInstanceRootExecution);
        // 获取任务用户
        Set<Long> assigneeUserIds = calculateTaskCandidateUsers(multiInstanceRootExecution, taskRule);
        // 设置任务集合变量
        String expressionText = String.format("%s_userList", taskRule.getTaskDefinitionKey());
        // 设置任务集合变量与任务关系
        multiInstanceRootExecution.setVariable(expressionText, assigneeUserIds);
        // 设置任务集合EL表达式
        this.collectionExpression = CommandContextUtil.getProcessEngineConfiguration().getExpressionManager()
            .createExpression(String.format("${%s}", expressionText));
        // 根据会签，或签类型，设置会签,或签条件
        if (BpmTaskAssignRuleTypeEnum.USER_SIGN.getType().equals(taskRule.getType())) {
            // 会签
            this.completionCondition = "${ nrOfInstances == nrOfCompletedInstances }";
        } else {
            // 或签
            this.completionCondition = "${ nrOfCompletedInstances == 1 }";
        }
        // 设置取出集合变量
        this.collectionElementVariable = "user";
        return super.createInstances(multiInstanceRootExecution);
    }

    @Override
    protected Object resolveCollection(DelegateExecution execution) {
        Object collection = null;
        if (EXPRESSION_TEXT_TEMPLATE.equals(this.collectionExpression.getExpressionText())) {
            // 查找任务信息
            BpmTaskAssignRuleDO taskRule = getTaskRule(execution);
            // 设置任务集合变量
            String expressionText = String.format("%s_userList", execution.getCurrentActivityId());
            // 获取任务用户
            Set<Long> assigneeUserIds = calculateTaskCandidateUsers(execution, taskRule);
            // 设置任务集合变量与任务关系
            execution.setVariable(expressionText, assigneeUserIds);
            // 设置任务集合EL表达式
            this.collectionExpression = CommandContextUtil.getProcessEngineConfiguration().getExpressionManager()
                .createExpression(String.format("${%s}", expressionText));
        }
        if (this.collectionExpression != null) {
            collection = this.collectionExpression.getValue(execution);
        } else if (this.collectionVariable != null) {
            collection = execution.getVariable(this.collectionVariable);
        } else if (this.collectionString != null) {
            collection = this.collectionString;
        }

        return collection;
    }

    private BpmTaskAssignRuleDO getTaskRule(DelegateExecution task) {
        List<BpmTaskAssignRuleDO> taskRules =
            bpmTaskRuleService.getTaskAssignRuleListByProcessDefinitionId(task.getProcessDefinitionId(),
                task.getCurrentActivityId());
        if (CollUtil.isEmpty(taskRules)) {
            throw new FlowableException(
                StrUtil.format("流程任务({}/{}/{}) 找不到符合的任务规则", task.getId(), task.getProcessDefinitionId(),
                    task.getCurrentActivityId()));
        }
        if (taskRules.size() > 1) {
            throw new FlowableException(
                StrUtil.format("流程任务({}/{}/{}) 找到过多任务规则({})", task.getId(), task.getProcessDefinitionId(),
                    task.getCurrentActivityId(), taskRules.size()));
        }
        return taskRules.get(0);
    }

    Set<Long> calculateTaskCandidateUsers(DelegateExecution task, BpmTaskAssignRuleDO rule) {
        Set<Long> assigneeUserIds = null;
        //        if (Objects.equals(BpmTaskAssignRuleTypeEnum.ROLE.getType(), rule.getType())) {
        //            assigneeUserIds = calculateTaskCandidateUsersByRole(task, rule);
        //        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.DEPT_MEMBER.getType(), rule.getType())) {
        //            assigneeUserIds = calculateTaskCandidateUsersByDeptMember(task, rule);
        //        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.DEPT_LEADER.getType(), rule.getType())) {
        //            assigneeUserIds = calculateTaskCandidateUsersByDeptLeader(task, rule);
        //        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.POST.getType(), rule.getType())) {
        //            assigneeUserIds = calculateTaskCandidateUsersByPost(task, rule);
        //        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.USER.getType(), rule.getType())) {
        //            assigneeUserIds = calculateTaskCandidateUsersByUser(task, rule);
        //        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.USER_GROUP.getType(), rule.getType())) {
        //            assigneeUserIds = calculateTaskCandidateUsersByUserGroup(task, rule);
        //        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.SCRIPT.getType(), rule.getType())) {
        //            assigneeUserIds = calculateTaskCandidateUsersByScript(task, rule);
        if (Objects.equals(BpmTaskAssignRuleTypeEnum.USER_SIGN.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersSignByUser(task, rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.USER_OR_SIGN.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersSignByUser(task, rule);
        }

        // 移除被禁用的用户
        removeDisableUsers(assigneeUserIds);
        // 如果候选人为空，抛出异常 TODO 芋艿：没候选人的策略选择。1 - 挂起；2 - 直接结束；3 - 强制一个兜底人
        if (CollUtil.isEmpty(assigneeUserIds)) {
            log.error("[calculateTaskCandidateUsers][流程任务({}/{}/{}) 任务规则({}) 找不到候选人]", task.getId(),
                task.getProcessDefinitionId(), task.getCurrentActivityId(), toJsonString(rule));
            throw exception(TASK_CREATE_FAIL_NO_CANDIDATE_USER);
        }
        return assigneeUserIds;
    }

    private Set<Long> calculateTaskCandidateUsersSignByUser(DelegateExecution task, BpmTaskAssignRuleDO rule) {
        return rule.getOptions();
    }

    @VisibleForTesting
    void removeDisableUsers(Set<Long> assigneeUserIds) {
        if (CollUtil.isEmpty(assigneeUserIds)) {
            return;
        }
        //TODO 芋艿 这里有数据权限的问题。默认会加上数据权限 dept_id IN (deptId). 导致查询不到数据
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(assigneeUserIds);
        assigneeUserIds.removeIf(id -> {
            AdminUserRespDTO user = userMap.get(id);
            return user == null || !CommonStatusEnum.ENABLE.getStatus().equals(user.getStatus());
        });
    }
}
