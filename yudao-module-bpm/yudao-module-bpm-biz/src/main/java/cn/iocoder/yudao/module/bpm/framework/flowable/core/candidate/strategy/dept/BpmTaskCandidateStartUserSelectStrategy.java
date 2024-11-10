package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.user.BpmTaskCandidateUserStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Task;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 发起人自选 {@link BpmTaskCandidateUserStrategy} 实现类
 *
 * @author 芋道源码
 */
@Component
public class BpmTaskCandidateStartUserSelectStrategy extends AbstractBpmTaskCandidateDeptLeaderStrategy {

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.START_USER_SELECT;
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
        Map<String, List<Long>> startUserSelectAssignees = FlowableUtils.getStartUserSelectAssignees(processInstance);
        Assert.notNull(startUserSelectAssignees, "流程实例({}) 的发起人自选审批人不能为空",
                execution.getProcessInstanceId());
        // 获得审批人
        List<Long> assignees = startUserSelectAssignees.get(execution.getCurrentActivityId());
        return new LinkedHashSet<>(assignees);
    }

    @Override
    public LinkedHashSet<Long> calculateUsersByActivity(BpmnModel bpmnModel, String activityId, String param,
                                                        Long startUserId, String processDefinitionId, Map<String, Object> processVariables) {
        if (processVariables == null) {
            return Sets.newLinkedHashSet();
        }
        Map<String, List<Long>> startUserSelectAssignees = FlowableUtils.getStartUserSelectAssignees(processVariables);
        if (startUserSelectAssignees == null) {
            return Sets.newLinkedHashSet();
        }
        // 获得审批人
        List<Long> assignees = startUserSelectAssignees.get(activityId);
        return new LinkedHashSet<>(assignees);
    }

    /**
     * 获得发起人自选审批人或抄送人的 Task 列表
     *
     * @param bpmnModel BPMN 模型
     * @return Task 列表
     */
    public static <T extends Task> List<T> getStartUserSelectUserTaskList(BpmnModel bpmnModel, Class<T> TaskClass ) {
        if (bpmnModel == null) {
            return null;
        }
        List<T> tasks = BpmnModelUtils.getBpmnModelElements(bpmnModel, TaskClass);
        if (CollUtil.isEmpty(tasks)) {
            return null;
        }
        tasks.removeIf(serviceTask -> !Objects.equals(BpmnModelUtils.parseCandidateStrategy(serviceTask),
                BpmTaskCandidateStrategyEnum.START_USER_SELECT.getStrategy()));
        return tasks;
    }

}
