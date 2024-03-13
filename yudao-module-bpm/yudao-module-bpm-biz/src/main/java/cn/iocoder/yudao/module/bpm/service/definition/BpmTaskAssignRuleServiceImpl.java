package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.module.bpm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.BpmTaskAssignScript;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.PostApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.permission.RoleApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;

import static cn.hutool.core.text.CharSequenceUtil.format;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * BPM 任务分配规则 Service 实现类
 */
@Service
@Validated
@Slf4j
public class BpmTaskAssignRuleServiceImpl implements BpmTaskAssignRuleService {

    @Resource
    private BpmUserGroupService userGroupService;
    @Resource
    @Lazy // 解决循环依赖
    private BpmProcessInstanceService processInstanceService;

    @Resource
    private RoleApi roleApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private PostApi postApi;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DictDataApi dictDataApi;
    @Resource
    private PermissionApi permissionApi;

    /**
     * 任务分配脚本
     */
    private Map<Long, BpmTaskAssignScript> scriptMap = Collections.emptyMap();

    @Resource
    public void setScripts(List<BpmTaskAssignScript> scripts) {
        this.scriptMap = convertMap(scripts, script -> script.getEnum().getId());
    }

    @Override
    public void checkTaskAssignRuleAllConfig(byte[] bpmnBytes) {
        BpmnModel bpmnModel = BpmnModelUtils.getBpmnModel(bpmnBytes);
        assert bpmnModel != null;
        List<UserTask> userTaskList = BpmnModelUtils.getBpmnModelElements(bpmnModel, UserTask.class);
        // 遍历所有的 UserTask，校验审批人配置
        userTaskList.forEach(userTask -> {
            // TODO 芋艿：assignType/assignOptions/, 枚举
            Integer type = NumberUtils.parseInt(userTask.getAttributeValue(BpmnModelConstants.NAMESPACE, "assignType"));
            String options = userTask.getAttributeValue(BpmnModelConstants.NAMESPACE, "assignOptions");
            if (type == null || StrUtil.isBlank(options)) {
                throw exception(MODEL_DEPLOY_FAIL_TASK_ASSIGN_RULE_NOT_CONFIG, userTask.getName());
            }
            validTaskAssignRuleOptions(type, StrUtils.splitToLong(options, ","));
        });
    }

    private void validTaskAssignRuleOptions(Integer type, Collection<Long> options) {
        if (Objects.equals(type, BpmTaskAssignRuleTypeEnum.ROLE.getType())) {
            roleApi.validRoleList(options);
        } else if (ObjectUtils.equalsAny(type, BpmTaskAssignRuleTypeEnum.DEPT_MEMBER.getType(),
                BpmTaskAssignRuleTypeEnum.DEPT_LEADER.getType())) {
            deptApi.validateDeptList(options);
        } else if (Objects.equals(type, BpmTaskAssignRuleTypeEnum.POST.getType())) {
            postApi.validPostList(options);
        } else if (Objects.equals(type, BpmTaskAssignRuleTypeEnum.USER.getType())) {
            adminUserApi.validateUserList(options);
        } else if (Objects.equals(type, BpmTaskAssignRuleTypeEnum.USER_GROUP.getType())) {
            userGroupService.validUserGroups(options);
        } else if (Objects.equals(type, BpmTaskAssignRuleTypeEnum.SCRIPT.getType())) {
            dictDataApi.validateDictDataList(DictTypeConstants.TASK_ASSIGN_SCRIPT,
                    CollectionUtils.convertSet(options, String::valueOf));
        } else {
            throw new IllegalArgumentException(format("未知的规则类型({})", type));
        }
    }

    @Override
    @DataPermission(enable = false) // 忽略数据权限，不然分配会存在问题
    public Set<Long> calculateTaskCandidateUsers(DelegateExecution execution) {
        // 1. 先从提前选好的审批人中获取
        List<Long> assignee = processInstanceService.getAssigneeByProcessInstanceIdAndTaskDefinitionKey(
                execution.getProcessInstanceId(), execution.getCurrentActivityId());
        if (CollUtil.isNotEmpty(assignee)) {
            // TODO @hai：new HashSet 即可
            return convertSet(assignee, Function.identity());
        }
        // 2. 通过分配规则，计算审批人
        return calculateTaskCandidateUsers0(execution);
    }

    @VisibleForTesting
    Set<Long> calculateTaskCandidateUsers0(DelegateExecution execution) {
        // 获得审批人配置
        // TODO 芋艿：assignType/assignOptions/, 枚举
        FlowElement flowElement = execution.getCurrentFlowElement();
        Integer type = NumberUtils.parseInt(flowElement.getAttributeValue(BpmnModelConstants.NAMESPACE, "assignType"));
        Set<Long> options = StrUtils.splitToLongSet(flowElement.getAttributeValue(BpmnModelConstants.NAMESPACE, "assignOptions"), ",");

        // 计算审批人
        Set<Long> assigneeUserIds = null;
        if (Objects.equals(BpmTaskAssignRuleTypeEnum.ROLE.getType(), type)) {
            assigneeUserIds = calculateTaskCandidateUsersByRole(options);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.DEPT_MEMBER.getType(), type)) {
            assigneeUserIds = calculateTaskCandidateUsersByDeptMember(options);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.DEPT_LEADER.getType(), type)) {
            assigneeUserIds = calculateTaskCandidateUsersByDeptLeader(options);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.POST.getType(), type)) {
            assigneeUserIds = calculateTaskCandidateUsersByPost(options);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.USER.getType(), type)) {
            assigneeUserIds = calculateTaskCandidateUsersByUser(options);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.USER_GROUP.getType(), type)) {
            assigneeUserIds = calculateTaskCandidateUsersByUserGroup(options);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.SCRIPT.getType(), type)) {
            assigneeUserIds = calculateTaskCandidateUsersByScript(execution, options);
        }

        // 移除被禁用的用户
        removeDisableUsers(assigneeUserIds);
        // 如果候选人为空，抛出异常
        if (CollUtil.isEmpty(assigneeUserIds)) {
            log.error("[calculateTaskCandidateUsers][流程任务({}/{}/{}) 任务规则({}/{}) 找不到候选人]", execution.getId(),
                    execution.getProcessDefinitionId(), execution.getCurrentActivityId(), type, options);
            throw exception(TASK_CREATE_FAIL_NO_CANDIDATE_USER);
        }
        return assigneeUserIds;
    }

    private Set<Long> calculateTaskCandidateUsersByRole(Set<Long> roleIds) {
        return permissionApi.getUserRoleIdListByRoleIds(roleIds);
    }

    private Set<Long> calculateTaskCandidateUsersByDeptMember(Set<Long> deptIds) {
        List<AdminUserRespDTO> users = adminUserApi.getUserListByDeptIds(deptIds);
        return convertSet(users, AdminUserRespDTO::getId);
    }

    private Set<Long> calculateTaskCandidateUsersByDeptLeader(Set<Long> deptIds) {
        List<DeptRespDTO> depts = deptApi.getDeptList(deptIds);
        return convertSet(depts, DeptRespDTO::getLeaderUserId);
    }

    private Set<Long> calculateTaskCandidateUsersByPost(Set<Long> postIds) {
        List<AdminUserRespDTO> users = adminUserApi.getUserListByPostIds(postIds);
        return convertSet(users, AdminUserRespDTO::getId);
    }

    private Set<Long> calculateTaskCandidateUsersByUser(Set<Long> userIds) {
        return userIds;
    }

    private Set<Long> calculateTaskCandidateUsersByUserGroup(Set<Long> groupIds) {
        List<BpmUserGroupDO> userGroups = userGroupService.getUserGroupList(groupIds);
        Set<Long> userIds = new HashSet<>();
        userGroups.forEach(group -> userIds.addAll(group.getMemberUserIds()));
        return userIds;
    }

    private Set<Long> calculateTaskCandidateUsersByScript(DelegateExecution execution, Set<Long> scriptIds) {
        // 获得对应的脚本
        List<BpmTaskAssignScript> scripts = new ArrayList<>(scriptIds.size());
        scriptIds.forEach(scriptId -> {
            BpmTaskAssignScript script = scriptMap.get(scriptId);
            if (script == null) {
                throw exception(TASK_ASSIGN_SCRIPT_NOT_EXISTS, scriptId);
            }
            scripts.add(script);
        });
        // 逐个计算任务
        Set<Long> userIds = new HashSet<>();
        scripts.forEach(script -> CollUtil.addAll(userIds, script.calculateTaskCandidateUsers(execution)));
        return userIds;
    }

    @VisibleForTesting
    void removeDisableUsers(Set<Long> assigneeUserIds) {
        if (CollUtil.isEmpty(assigneeUserIds)) {
            return;
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(assigneeUserIds);
        assigneeUserIds.removeIf(id -> {
            AdminUserRespDTO user = userMap.get(id);
            return user == null || !CommonStatusEnum.ENABLE.getStatus().equals(user.getStatus());
        });
    }

}
