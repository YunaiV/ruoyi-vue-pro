package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.expression;

import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 分配给发起人审批的 Expression 流程表达式
 *
 * @author 芋道源码
 */
@Component
public class BpmTaskAssignStartUserExpression {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    /**
     * 计算审批的候选人
     *
     * @param execution 流程执行实体
     * @return 发起人
     */
    public Set<Long> calculateUsers(ExecutionEntityImpl execution) {
        ProcessInstance processInstance = processInstanceService.getProcessInstance(execution.getProcessInstanceId());
        Long startUserId = NumberUtils.parseLong(processInstance.getStartUserId());
        return SetUtils.asSet(startUserId);
    }

}
