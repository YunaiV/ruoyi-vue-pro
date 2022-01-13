package cn.iocoder.yudao.adminserver.modules.bpm.service.definition.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule.BpmTaskAssignRuleCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule.BpmTaskAssignRuleRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.definition.BpmTaskAssignRuleConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmTaskAssignRuleDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition.BpmTaskAssignRuleMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmTaskAssignRuleService;
import cn.iocoder.yudao.adminserver.modules.system.service.permission.SysRoleService;
import cn.iocoder.yudao.framework.activiti.core.util.ActivitiUtils;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

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
    private SysRoleService roleService;

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
        List<UserTask> userTasks = ActivitiUtils.getBpmnModelElements(model, UserTask.class);
        if (CollUtil.isEmpty(userTasks)) {
            return Collections.emptyList();
        }

        // 转换数据
        return BpmTaskAssignRuleConvert.INSTANCE.convertList(userTasks, rules);
    }

    @Override
    public Long createTaskAssignRule(BpmTaskAssignRuleCreateReqVO reqVO) {
        // 校验参数
        validTaskAssignRuleOptions(reqVO.getType(), reqVO.getOptions());
        // 校验是否已经配置
        BpmTaskAssignRuleDO existRule = taskRuleMapper.selectListByModelIdAndTaskDefinitionKey(
                reqVO.getModelId(), reqVO.getTaskDefinitionKey());
        if (existRule != null) {
            throw exception(BpmErrorCodeConstants.TASK_ASSIGN_RULE_EXISTS,
                    reqVO.getModelId(), reqVO.getTaskDefinitionKey());
        }

        // 存储
        BpmTaskAssignRuleDO rule = BpmTaskAssignRuleConvert.INSTANCE.convert(reqVO)
                .setProcessDefinitionId(BpmTaskAssignRuleDO.PROCESS_DEFINITION_ID_NULL); // 只有流程模型，才允许新建
        taskRuleMapper.insert(rule);
        return rule.getId();
    }

    private void validTaskAssignRuleOptions(Integer type, Set<Long> options) {
        if (Objects.equals(type, BpmTaskAssignRuleTypeEnum.ROLE.getType())) {
            roleService.validRoles(options);
            return;
        }

    }

}
