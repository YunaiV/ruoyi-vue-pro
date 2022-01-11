package cn.iocoder.yudao.adminserver.modules.bpm.service.definition.impl;

import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmTaskRuleDO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition.BpmTaskRuleMapper;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmTaskRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * BPM 任务规则 Service 实现类
 */
@Service
@Validated
@Slf4j
public class BpmTaskRuleServiceImpl implements BpmTaskRuleService {

    @Resource
    private BpmTaskRuleMapper taskRuleMapper;

    @Override
    public List<BpmTaskRuleDO> getTaskRulesByProcessDefinitionId(String processDefinitionId,
                                                                 String taskDefinitionKey) {
        return taskRuleMapper.selectListByProcessDefinitionId(processDefinitionId, taskDefinitionKey);
    }

    @Override
    public List<BpmTaskRuleDO> getTaskRulesByModelId(Long modelId) {
        return null;
    }

}
