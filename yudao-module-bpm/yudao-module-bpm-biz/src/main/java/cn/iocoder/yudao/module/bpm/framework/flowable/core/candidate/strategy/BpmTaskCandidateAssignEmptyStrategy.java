package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy;

import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskAssignEmptyHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 审批人为空 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author kyle
 */
@Component
public class BpmTaskCandidateAssignEmptyStrategy extends BpmTaskCandidateAbstractStrategy {

    public BpmTaskCandidateAssignEmptyStrategy(AdminUserApi adminUserApi) {
        super(adminUserApi);
    }

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.ASSIGN_EMPTY;
    }

    @Override
    public void validateParam(String param) {
    }

    @Override
    public Set<Long> calculateUsers(DelegateExecution execution, String param) {
        // 情况一：指定人员审批
        Integer assignEmptyHandlerType = BpmnModelUtils.parseAssignEmptyHandlerType(execution.getCurrentFlowElement());
        if (Objects.equals(assignEmptyHandlerType, BpmUserTaskAssignEmptyHandlerTypeEnum.ASSIGN_USER.getType())) {
            Set<Long> users = new HashSet<>(BpmnModelUtils.parseAssignEmptyHandlerUserIds(execution.getCurrentFlowElement()));
            removeDisableUsers(users);
            return users;
        }

        // 情况二：流程管理员
        if (Objects.equals(assignEmptyHandlerType, BpmUserTaskAssignEmptyHandlerTypeEnum.ASSIGN_ADMIN.getType())) {
            // TODO 芋艿：需要等待流程实例的管理员支持
            throw new UnsupportedOperationException("暂时实现！！！");
        }

        // 都不满足，还是返回空
        return new HashSet<>();
    }

}