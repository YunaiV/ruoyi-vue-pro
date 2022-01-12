package cn.iocoder.yudao.adminserver.modules.bpm.service.definition.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule.BpmTaskAssignRuleRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.definition.BpmTaskAssignRuleConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmTaskAssignRuleDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition.BpmTaskAssignRuleMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmTaskAssignRuleService;
import cn.iocoder.yudao.framework.activiti.core.util.ActivitiUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

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

    @Override
    public List<BpmTaskAssignRuleDO> getTaskAssignRulesByProcessDefinitionId(String processDefinitionId,
                                                                             String taskDefinitionKey) {
        return taskRuleMapper.selectListByProcessDefinitionId(processDefinitionId, taskDefinitionKey);
    }

    @Override
    public List<BpmTaskAssignRuleDO> getTaskAssignRulesByModelId(String modelId) {
        return taskRuleMapper.selectListByModelId(modelId);
    }

    @Override
    public List<BpmTaskAssignRuleRespVO> getTaskAssignRules(String modelId, String processDefinitionId) {
        // 获得规则
        List<BpmTaskAssignRuleDO> rules = Collections.emptyList();
        BpmnModel model = null;
        if (StrUtil.isNotEmpty(modelId)) {
            rules = getTaskAssignRulesByModelId(modelId);
            model = modelService.getBpmnModel(modelId);
        } else if (StrUtil.isNotEmpty(processDefinitionId)) {
            rules = getTaskAssignRulesByProcessDefinitionId(processDefinitionId, null);
            model = processDefinitionService.getBpmnModel(processDefinitionId);
        }
        if (CollUtil.isEmpty(rules) || model == null) {
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

}
