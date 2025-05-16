package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.user.BpmTaskCandidateUserStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 审批人自选 {@link BpmTaskCandidateUserStrategy} 实现类
 * 审批人在审批时选择下一个节点的审批人
 *
 * @author smallNorthLee
 */
@Component
public class BpmTaskCandidateApproveUserSelectStrategy extends AbstractBpmTaskCandidateDeptLeaderStrategy {

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.APPROVE_USER_SELECT;
    }

    @Override
    public void validateParam(String param) {}

    @Override
    public boolean isParamRequired() {
        return false;
    }

    @Override
    public LinkedHashSet<Long> calculateUsersByTask(DelegateExecution execution, String param) {
        ProcessInstance processInstance = processInstanceService.getProcessInstance(execution.getProcessInstanceId());
        Assert.notNull(processInstance, "流程实例({})不能为空", execution.getProcessInstanceId());
        Map<String, List<Long>> approveUserSelectAssignees = FlowableUtils.getApproveUserSelectAssignees(processInstance);
        Assert.notNull(approveUserSelectAssignees, "流程实例({}) 的下一个执行节点审批人不能为空",
                execution.getProcessInstanceId());
        if (approveUserSelectAssignees == null) {
            return Sets.newLinkedHashSet();
        }
        // 获得审批人
        List<Long> assignees = approveUserSelectAssignees.get(execution.getCurrentActivityId());
        return CollUtil.isNotEmpty(assignees) ? new LinkedHashSet<>(assignees) : Sets.newLinkedHashSet();
    }

    @Override
    public LinkedHashSet<Long> calculateUsersByActivity(BpmnModel bpmnModel, String activityId, String param,
                                                        Long startUserId, String processDefinitionId, Map<String, Object> processVariables) {
        if (processVariables == null) {
            return Sets.newLinkedHashSet();
        }
        // 流程预测时会使用，允许审批人为空，如果为空前端会弹出提示选择下一个节点审批人，避免流程无法进行，审批时会真正校验节点是否配置审批人
        Map<String, List<Long>> approveUserSelectAssignees = FlowableUtils.getApproveUserSelectAssignees(processVariables);
        if (approveUserSelectAssignees == null) {
            return Sets.newLinkedHashSet();
        }
        // 获得审批人
        List<Long> assignees = approveUserSelectAssignees.get(activityId);
        return CollUtil.isNotEmpty(assignees) ? new LinkedHashSet<>(assignees) : Sets.newLinkedHashSet();
    }

}
