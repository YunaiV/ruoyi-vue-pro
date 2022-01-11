package cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmTaskRuleDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;

import java.util.List;

@Mapper
public interface BpmTaskRuleMapper extends BaseMapperX<BpmTaskRuleDO> {

    default List<BpmTaskRuleDO> selectListByProcessDefinitionId(String processDefinitionId,
                                                                @Nullable String taskDefinitionKey) {
        return selectList(new QueryWrapperX<BpmTaskRuleDO>()
                .eq("process_definition_id", processDefinitionId)
                .eqIfPresent("task_definition_key", taskDefinitionKey));
    }

}
