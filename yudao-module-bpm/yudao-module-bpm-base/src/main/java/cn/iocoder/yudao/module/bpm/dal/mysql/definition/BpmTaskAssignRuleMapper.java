package cn.iocoder.yudao.module.bpm.dal.mysql.definition;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmTaskAssignRuleDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;

import java.util.List;

@Mapper
public interface BpmTaskAssignRuleMapper extends BaseMapperX<BpmTaskAssignRuleDO> {

    default List<BpmTaskAssignRuleDO> selectListByProcessDefinitionId(String processDefinitionId,
                                                                      @Nullable String taskDefinitionKey) {
        return selectList(new LambdaQueryWrapperX<BpmTaskAssignRuleDO>()
                .eq(BpmTaskAssignRuleDO::getProcessDefinitionId, processDefinitionId)
                .eqIfPresent(BpmTaskAssignRuleDO::getTaskDefinitionKey, taskDefinitionKey));
    }

    default List<BpmTaskAssignRuleDO> selectListByModelId(String modelId) {
        return selectList(new LambdaQueryWrapperX<BpmTaskAssignRuleDO>()
                .eq(BpmTaskAssignRuleDO::getModelId, modelId)
                .eq(BpmTaskAssignRuleDO::getProcessDefinitionId, BpmTaskAssignRuleDO.PROCESS_DEFINITION_ID_NULL));
    }

    default BpmTaskAssignRuleDO selectListByModelIdAndTaskDefinitionKey(String modelId,
                                                                        String taskDefinitionKey) {
        return selectOne(new LambdaQueryWrapperX<BpmTaskAssignRuleDO>()
                .eq(BpmTaskAssignRuleDO::getModelId, modelId)
                .eq(BpmTaskAssignRuleDO::getProcessDefinitionId, BpmTaskAssignRuleDO.PROCESS_DEFINITION_ID_NULL)
                .eq(BpmTaskAssignRuleDO::getTaskDefinitionKey, taskDefinitionKey));
    }

}
