package cn.iocoder.yudao.module.bpm.service.definition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.rule.BpmTaskAssignRuleCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.rule.BpmTaskAssignRuleRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.rule.BpmTaskAssignRuleUpdateReqVO;
import cn.iocoder.yudao.module.bpm.convert.definition.BpmTaskAssignRuleConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmTaskAssignRuleDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmTaskAssignRuleMapper;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.DictTypeConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.BpmTaskAssignScript;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.PostApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.permission.RoleApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;

import static cn.hutool.core.text.CharSequenceUtil.format;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * BPM 任务分配规则 Service 实现类
 */
@Service
@Validated
@Slf4j
public class BpmTaskAssignRuleServiceImpl implements BpmTaskAssignRuleService {

    @Resource
    private BpmTaskAssignRuleMapper taskRuleMapper;
    @Resource
    @Lazy // 解决循环依赖
    private BpmModelService modelService;
    @Resource
    @Lazy // 解决循环依赖
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmUserGroupService userGroupService;
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
    public List<BpmTaskAssignRuleDO> getTaskAssignRuleListByProcessDefinitionId(String processDefinitionId,
        String taskDefinitionKey) {
        return taskRuleMapper.selectListByProcessDefinitionId(processDefinitionId, taskDefinitionKey);
    }

    @Override
    public List<BpmTaskAssignRuleDO> getTaskAssignRuleListByModelId(String modelId) {
        return taskRuleMapper.selectListByModelId(modelId);
    }

    @Override
    public List<BpmTaskAssignRuleRespVO> getTaskAssignRuleList(String modelId, String processDefinitionId) {
        // 获得规则
        List<BpmTaskAssignRuleDO> rules = Collections.emptyList();
        BpmnModel model = null;
        if (StrUtil.isNotEmpty(modelId)) {
            rules = getTaskAssignRuleListByModelId(modelId);
            model = modelService.getBpmnModel(modelId);
        } else if (StrUtil.isNotEmpty(processDefinitionId)) {
            rules = getTaskAssignRuleListByProcessDefinitionId(processDefinitionId, null);
            model = processDefinitionService.getBpmnModel(processDefinitionId);
        }
        if (model == null) {
            return Collections.emptyList();
        }
        // 获得用户任务，只有用户任务才可以设置分配规则
        List<UserTask> userTasks = FlowableUtils.getBpmnModelElements(model, UserTask.class);
        if (CollUtil.isEmpty(userTasks)) {
            return Collections.emptyList();
        }
        // 转换数据
        return BpmTaskAssignRuleConvert.INSTANCE.convertList(userTasks, rules);
    }

    @Override
    public Long createTaskAssignRule(@Valid BpmTaskAssignRuleCreateReqVO reqVO) {
        // 校验参数
        validTaskAssignRuleOptions(reqVO.getType(), reqVO.getOptions());
        // 校验是否已经配置
        BpmTaskAssignRuleDO existRule =
            taskRuleMapper.selectListByModelIdAndTaskDefinitionKey(reqVO.getModelId(), reqVO.getTaskDefinitionKey());
        if (existRule != null) {
            throw exception(TASK_ASSIGN_RULE_EXISTS, reqVO.getModelId(), reqVO.getTaskDefinitionKey());
        }

        // 存储
        BpmTaskAssignRuleDO rule = BpmTaskAssignRuleConvert.INSTANCE.convert(reqVO)
            .setProcessDefinitionId(BpmTaskAssignRuleDO.PROCESS_DEFINITION_ID_NULL); // 只有流程模型，才允许新建
        taskRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    public void updateTaskAssignRule(@Valid BpmTaskAssignRuleUpdateReqVO reqVO) {
        // 校验参数
        validTaskAssignRuleOptions(reqVO.getType(), reqVO.getOptions());
        // 校验是否存在
        BpmTaskAssignRuleDO existRule = taskRuleMapper.selectById(reqVO.getId());
        if (existRule == null) {
            throw exception(TASK_ASSIGN_RULE_NOT_EXISTS);
        }
        // 只允许修改流程模型的规则
        if (!Objects.equals(BpmTaskAssignRuleDO.PROCESS_DEFINITION_ID_NULL, existRule.getProcessDefinitionId())) {
            throw exception(TASK_UPDATE_FAIL_NOT_MODEL);
        }

        // 执行更新
        taskRuleMapper.updateById(BpmTaskAssignRuleConvert.INSTANCE.convert(reqVO));
    }

    @Override
    public boolean isTaskAssignRulesEquals(String modelId, String processDefinitionId) {
        // 调用 VO 接口的原因是，过滤掉流程模型不需要的规则，保持和 copyTaskAssignRules 方法的一致性
        List<BpmTaskAssignRuleRespVO> modelRules = getTaskAssignRuleList(modelId, null);
        List<BpmTaskAssignRuleRespVO> processInstanceRules = getTaskAssignRuleList(null, processDefinitionId);
        if (modelRules.size() != processInstanceRules.size()) {
            return false;
        }

        // 遍历，匹配对应的规则
        Map<String, BpmTaskAssignRuleRespVO> processInstanceRuleMap =
            CollectionUtils.convertMap(processInstanceRules, BpmTaskAssignRuleRespVO::getTaskDefinitionKey);
        for (BpmTaskAssignRuleRespVO modelRule : modelRules) {
            BpmTaskAssignRuleRespVO processInstanceRule = processInstanceRuleMap.get(modelRule.getTaskDefinitionKey());
            if (processInstanceRule == null) {
                return false;
            }
            if (!ObjectUtil.equals(modelRule.getType(), processInstanceRule.getType()) || !ObjectUtil.equal(
                modelRule.getOptions(), processInstanceRule.getOptions())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void copyTaskAssignRules(String fromModelId, String toProcessDefinitionId) {
        List<BpmTaskAssignRuleRespVO> rules = getTaskAssignRuleList(fromModelId, null);
        if (CollUtil.isEmpty(rules)) {
            return;
        }
        // 开始复制
        List<BpmTaskAssignRuleDO> newRules = BpmTaskAssignRuleConvert.INSTANCE.convertList2(rules);
        newRules.forEach(rule -> rule.setProcessDefinitionId(toProcessDefinitionId).setId(null).setCreateTime(null)
            .setUpdateTime(null));
        taskRuleMapper.insertBatch(newRules);
    }

    @Override
    public void checkTaskAssignRuleAllConfig(String id) {
        // 一个用户任务都没配置，所以无需配置规则
        List<BpmTaskAssignRuleRespVO> taskAssignRules = getTaskAssignRuleList(id, null);
        if (CollUtil.isEmpty(taskAssignRules)) {
            return;
        }
        // 校验未配置规则的任务
        taskAssignRules.forEach(rule -> {
            if (CollUtil.isEmpty(rule.getOptions())) {
                throw exception(MODEL_DEPLOY_FAIL_TASK_ASSIGN_RULE_NOT_CONFIG, rule.getTaskDefinitionName());
            }
        });
    }

    private void validTaskAssignRuleOptions(Integer type, Set<Long> options) {
        if (Objects.equals(type, BpmTaskAssignRuleTypeEnum.ROLE.getType())) {
            roleApi.validRoles(options);
        } else if (ObjectUtils.equalsAny(type, BpmTaskAssignRuleTypeEnum.DEPT_MEMBER.getType(),
            BpmTaskAssignRuleTypeEnum.DEPT_LEADER.getType())) {
            deptApi.validDepts(options);
        } else if (Objects.equals(type, BpmTaskAssignRuleTypeEnum.POST.getType())) {
            postApi.validPosts(options);
        } else if (Objects.equals(type, BpmTaskAssignRuleTypeEnum.USER.getType())) {
            adminUserApi.validUsers(options);
        } else if (Objects.equals(type, BpmTaskAssignRuleTypeEnum.USER_GROUP.getType())) {
            userGroupService.validUserGroups(options);
        } else if (Objects.equals(type, BpmTaskAssignRuleTypeEnum.SCRIPT.getType())) {
            dictDataApi.validDictDatas(DictTypeConstants.TASK_ASSIGN_SCRIPT,
                CollectionUtils.convertSet(options, String::valueOf));
        } else {
            throw new IllegalArgumentException(format("未知的规则类型({})", type));
        }
    }

    @Override
    @DataPermission(enable = false) // 忽略数据权限，不然分配会存在问题
    public Set<Long> calculateTaskCandidateUsers(DelegateExecution execution) {
        BpmTaskAssignRuleDO rule = getTaskRule(execution);
        return calculateTaskCandidateUsers(execution, rule);
    }

    @VisibleForTesting
    BpmTaskAssignRuleDO getTaskRule(DelegateExecution execution) {
        List<BpmTaskAssignRuleDO> taskRules = getTaskAssignRuleListByProcessDefinitionId(
                execution.getProcessDefinitionId(), execution.getCurrentActivityId());
        if (CollUtil.isEmpty(taskRules)) {
            throw new FlowableException(format("流程任务({}/{}/{}) 找不到符合的任务规则",
                    execution.getId(), execution.getProcessDefinitionId(), execution.getCurrentActivityId()));
        }
        if (taskRules.size() > 1) {
            throw new FlowableException(format("流程任务({}/{}/{}) 找到过多任务规则({})",
                    execution.getId(), execution.getProcessDefinitionId(), execution.getCurrentActivityId()));
        }
        return taskRules.get(0);
    }

    @VisibleForTesting
    Set<Long> calculateTaskCandidateUsers(DelegateExecution execution, BpmTaskAssignRuleDO rule) {
        Set<Long> assigneeUserIds = null;
        if (Objects.equals(BpmTaskAssignRuleTypeEnum.ROLE.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByRole(rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.DEPT_MEMBER.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByDeptMember(rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.DEPT_LEADER.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByDeptLeader(rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.POST.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByPost(rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.USER.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByUser(rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.USER_GROUP.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByUserGroup(rule);
        } else if (Objects.equals(BpmTaskAssignRuleTypeEnum.SCRIPT.getType(), rule.getType())) {
            assigneeUserIds = calculateTaskCandidateUsersByScript(execution, rule);
        }

        // 移除被禁用的用户
        removeDisableUsers(assigneeUserIds);
        // 如果候选人为空，抛出异常
        if (CollUtil.isEmpty(assigneeUserIds)) {
            log.error("[calculateTaskCandidateUsers][流程任务({}/{}/{}) 任务规则({}) 找不到候选人]", execution.getId(),
                    execution.getProcessDefinitionId(), execution.getCurrentActivityId(), toJsonString(rule));
            throw exception(TASK_CREATE_FAIL_NO_CANDIDATE_USER);
        }
        return assigneeUserIds;
    }

    private Set<Long> calculateTaskCandidateUsersByRole(BpmTaskAssignRuleDO rule) {
        return permissionApi.getUserRoleIdListByRoleIds(rule.getOptions());
    }

    private Set<Long> calculateTaskCandidateUsersByDeptMember(BpmTaskAssignRuleDO rule) {
        List<AdminUserRespDTO> users = adminUserApi.getUsersByDeptIds(rule.getOptions());
        return convertSet(users, AdminUserRespDTO::getId);
    }

    private Set<Long> calculateTaskCandidateUsersByDeptLeader(BpmTaskAssignRuleDO rule) {
        List<DeptRespDTO> depts = deptApi.getDepts(rule.getOptions());
        return convertSet(depts, DeptRespDTO::getLeaderUserId);
    }

    private Set<Long> calculateTaskCandidateUsersByPost(BpmTaskAssignRuleDO rule) {
        List<AdminUserRespDTO> users = adminUserApi.getUsersByPostIds(rule.getOptions());
        return convertSet(users, AdminUserRespDTO::getId);
    }

    private Set<Long> calculateTaskCandidateUsersByUser(BpmTaskAssignRuleDO rule) {
        return rule.getOptions();
    }

    private Set<Long> calculateTaskCandidateUsersByUserGroup(BpmTaskAssignRuleDO rule) {
        List<BpmUserGroupDO> userGroups = userGroupService.getUserGroupList(rule.getOptions());
        Set<Long> userIds = new HashSet<>();
        userGroups.forEach(group -> userIds.addAll(group.getMemberUserIds()));
        return userIds;
    }

    private Set<Long> calculateTaskCandidateUsersByScript(DelegateExecution execution, BpmTaskAssignRuleDO rule) {
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
