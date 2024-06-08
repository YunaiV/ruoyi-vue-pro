package cn.iocoder.yudao.module.bpm.framework.flowable.core.custom.delegate;

import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceCopyService;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 处理抄送用户的代理
 *
 * @author jason
 */
@Service
public class CopyUserDelegate implements JavaDelegate  {

    @Resource
    private BpmTaskCandidateInvoker taskCandidateInvoker;
    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;

    @Override
    public void execute(DelegateExecution execution) {
        // TODO @芋艿：可能要考虑，系统抄送，没有 taskId 的情况。
        Set<Long> userIds = taskCandidateInvoker.calculateUsers(execution);
        FlowElement currentFlowElement = execution.getCurrentFlowElement();
        processInstanceCopyService.createProcessInstanceCopy(userIds, execution.getProcessInstanceId(),
                currentFlowElement.getId(), currentFlowElement.getName());
    }

}
