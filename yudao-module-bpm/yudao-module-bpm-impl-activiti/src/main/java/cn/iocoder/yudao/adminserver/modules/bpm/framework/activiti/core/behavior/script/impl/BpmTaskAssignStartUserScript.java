package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior.script.impl;

import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskRuleScriptEnum;
import cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior.script.BpmTaskAssignScript;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 分配给发起人审批的 Script 实现类
 *
 * @author 芋道源码
 */
@Component
public class BpmTaskAssignStartUserScript implements BpmTaskAssignScript {

    @Override
    public Set<Long> calculateTaskCandidateUsers(TaskEntity task) {
        Long userId = Long.parseLong(task.getProcessInstance().getStartUserId());
        return SetUtils.asSet(userId);
    }

    @Override
    public BpmTaskRuleScriptEnum getEnum() {
        return BpmTaskRuleScriptEnum.START_USER;
    }

}
