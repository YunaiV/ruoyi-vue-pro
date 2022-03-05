package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.datapermission.core.dept.rule.DeptDataPermissionRule;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmTaskAssignRuleDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.BpmTaskAssignScript;
import cn.iocoder.yudao.module.bpm.service.definition.BpmTaskAssignRuleService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmUserGroupService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.annotations.VisibleForTesting;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.util.TaskHelper;
import org.flowable.task.service.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.TASK_ASSIGN_SCRIPT_NOT_EXISTS;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.TASK_CREATE_FAIL_NO_CANDIDATE_USER;

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
    @Setter
    private BpmUserGroupService userGroupService;
    @Setter
    private DeptApi deptApi;
    @Setter
    private AdminUserApi adminUserApi;
    @Setter
    private PermissionApi permissionApi;

    /**
     * 任务分配脚本
     */
    private Map<Long, BpmTaskAssignScript> scriptMap = Collections.emptyMap();

    public BpmUserTaskActivityBehavior(UserTask userTask) {
        super(userTask);
    }
    public void setScripts(List<BpmTaskAssignScript> scripts) {
        this.scriptMap = convertMap(scripts, script -> script.getEnum().getId());
    }

    @Override
    protected void handleAssignments(TaskService taskService, String assignee, String owner, List<String> candidateUsers, List<String> candidateGroups, TaskEntity task, ExpressionManager expressionManager, DelegateExecution execution, ProcessEngineConfigurationImpl processEngineConfiguration) {
        // 第一步，获得任务的规则
        BpmTaskAssignRuleDO rule = getTaskRule(task);
        // 第二步，获得任务的候选用户们
        Set<Long> candidateUserIds = calculateTaskCandidateUsers(task, rule);
        // 第三步，设置一个作为负责人
        Long assigneeUserId = chooseTaskAssignee(candidateUserIds);
        TaskHelper.changeTaskAssignee(task, String.valueOf(assigneeUserId));
    }

    private BpmTaskAssignRuleDO getTaskRule(TaskEntity task) {
        List<BpmTaskAssignRuleDO> taskRules = bpmTaskRuleService.getTaskAssignRuleListByProcessDefinitionId(task.getProcessDefinitionId(),
                task.getTaskDefinitionKey());
        if (CollUtil.isEmpty(taskRules)) {
            throw new FlowableException(StrUtil.format("流程任务({}/{}/{}) 找不到符合的任务规则",
                    task.getId(), task.getProcessDefinitionId(), task.getTaskDefinitionKey()));
        }
        if (taskRules.size() > 1) {
            throw new FlowableException(StrUtil.format("流程任务({}/{}/{}) 找到过多任务规则({})",
                    task.getId(), task.getProcessDefinitionId(), task.getTaskDefinitionKey(), taskRules.size()));
        }
        return taskRules.get(0);
    }

    @VisibleForTesting
    Set<Long> calculateTaskCandidateUsers(TaskEntity task, BpmTaskAssignRuleDO rule) {
        Set<Long> assigneeUserIds = null;
        if (Objects.equals(BpmTaskAssignRuleTypeEnum.ROLE.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByRole(task, rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.DEPT_MEMBER.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByDeptMember(task, rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.DEPT_LEADER.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByDeptLeader(task, rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.POST.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByPost(task, rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.USER.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByUser(task, rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.USER_GROUP.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByUserGroup(task, rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.SCRIPT.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByScript(task, rule);
        }

        // 移除被禁用的用户
        removeDisableUsers(assigneeUserIds);
        // 如果候选人为空，抛出异常 TODO 芋艿：没候选人的策略选择。1 - 挂起；2 - 直接结束；3 - 强制一个兜底人
        if (CollUtil.isEmpty(assigneeUserIds)) {
            log.error("[calculateTaskCandidateUsers][流程任务({}/{}/{}) 任务规则({}) 找不到候选人]",
                    task.getId(), task.getProcessDefinitionId(), task.getTaskDefinitionKey(), toJsonString(rule));
            throw exception(TASK_CREATE_FAIL_NO_CANDIDATE_USER);
        }
        return assigneeUserIds;
    }

    private Set<Long> calculateTaskCandidateUsersByRole(TaskEntity task, BpmTaskAssignRuleDO rule) {
        return permissionApi.getUserRoleIdListByRoleIds(rule.getOptions());
    }

    private Set<Long> calculateTaskCandidateUsersByDeptMember(TaskEntity task, BpmTaskAssignRuleDO rule) {
        List<AdminUserRespDTO> users = adminUserApi.getUsersByDeptIds(rule.getOptions());
        return convertSet(users, AdminUserRespDTO::getId);
    }

    private Set<Long> calculateTaskCandidateUsersByDeptLeader(TaskEntity task, BpmTaskAssignRuleDO rule) {
        List<DeptRespDTO> depts = deptApi.getDepts(rule.getOptions());
        return convertSet(depts, DeptRespDTO::getLeaderUserId);
    }

    private Set<Long> calculateTaskCandidateUsersByPost(TaskEntity task, BpmTaskAssignRuleDO rule) {
        List<AdminUserRespDTO> users = adminUserApi.getUsersByPostIds(rule.getOptions());
        return convertSet(users, AdminUserRespDTO::getId);
    }

    private Set<Long> calculateTaskCandidateUsersByUser(TaskEntity task, BpmTaskAssignRuleDO rule) {
        return rule.getOptions();
    }

    private Set<Long> calculateTaskCandidateUsersByUserGroup(TaskEntity task, BpmTaskAssignRuleDO rule) {
        List<BpmUserGroupDO> userGroups = userGroupService.getUserGroupList(rule.getOptions());
        Set<Long> userIds = new HashSet<>();
        userGroups.forEach(group -> userIds.addAll(group.getMemberUserIds()));
        return userIds;
    }

    private Set<Long> calculateTaskCandidateUsersByScript(TaskEntity task, BpmTaskAssignRuleDO rule) {
        // 获得对应的脚本
        List<BpmTaskAssignScript> scripts = new ArrayList<>(rule.getOptions().size());
        rule.getOptions().forEach(id -> {
            BpmTaskAssignScript script = scriptMap.get(id);
            if (script == null) {
                throw exception(TASK_ASSIGN_SCRIPT_NOT_EXISTS, id);
            }
            scripts.add(script);
        });
        // 逐个计算任务
        Set<Long> userIds = new HashSet<>();
        scripts.forEach(script -> CollUtil.addAll(userIds, script.calculateTaskCandidateUsers(task)));
        return userIds;
    }

    private Long chooseTaskAssignee(Set<Long> candidateUserIds) {
        // TODO 芋艿：未来可以优化下，改成轮询的策略
        int index = RandomUtil.randomInt(candidateUserIds.size());
        return CollUtil.get(candidateUserIds, index);
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
