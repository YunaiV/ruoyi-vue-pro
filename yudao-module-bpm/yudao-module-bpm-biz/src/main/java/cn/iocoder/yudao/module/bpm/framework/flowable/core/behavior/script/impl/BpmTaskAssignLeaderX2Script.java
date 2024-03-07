package cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.script.impl;

import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskRuleScriptEnum;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 分配给发起人的二级 Leader 审批的 Script 实现类
 *
 * @author 芋道源码
 */
@Component
public class BpmTaskAssignLeaderX2Script extends BpmTaskAssignLeaderAbstractScript {

    @Override
    public Set<Long> calculateTaskCandidateUsers(DelegateExecution execution) {
        return calculateTaskCandidateUsers(execution, 2);
    }

    @Override
    public BpmTaskRuleScriptEnum getEnum() {
        return BpmTaskRuleScriptEnum.LEADER_X2;
    }

}
