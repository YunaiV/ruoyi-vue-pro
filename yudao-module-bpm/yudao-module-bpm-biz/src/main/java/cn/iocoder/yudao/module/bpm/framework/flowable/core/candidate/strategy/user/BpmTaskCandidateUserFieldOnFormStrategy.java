package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.user;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;

/**
 * 表单内用户字段 {@link BpmTaskCandidateUserStrategy} 实现类
 *
 * @author jason
 */
@Component
public class BpmTaskCandidateUserFieldOnFormStrategy implements BpmTaskCandidateStrategy  {

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.USER_FIELD_ON_FORM;
    }

    @Override
    public void validateParam(String param) {
        Assert.notEmpty(param, "表单内用户字段不能为空");
    }

    @Override
    public Set<Long> calculateUsersByTask(DelegateExecution execution, String param) {
        Object result =  execution.getVariable(param);
        return Convert.toSet(Long.class, result);
    }

    @Override
    public Set<Long> calculateUsersByActivity(BpmnModel bpmnModel, String activityId,
                                              String param, Long startUserId, String processDefinitionId,
                                              Map<String, Object> processVariables) {
        Long result = MapUtil.getLong(processVariables, param);
        return result != null ? asSet(result) : new HashSet<>();
    }

}
