package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 仿钉钉快搭各个节点 Service TODO @jason：注释要有空行哈；
 * @author jason
 */
@Service
public class BpmSimpleNodeService {

    @Resource
    private BpmTaskCandidateInvoker taskCandidateInvoker;
    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;

    /**
     * 仿钉钉快搭抄送 TODO @jason：注释要有空行哈；
     * @param execution 执行的任务(ScriptTask)
     */
    public Boolean copy(DelegateExecution execution) {
        // TODO @芋艿：可能要考虑，系统抄送，没有 taskId 的情况。
        Set<Long> userIds = taskCandidateInvoker.calculateUsers(execution);
        FlowElement currentFlowElement = execution.getCurrentFlowElement();
        processInstanceCopyService.createProcessInstanceCopy(userIds, execution.getProcessInstanceId(),
                currentFlowElement.getId(), currentFlowElement.getName());
        return Boolean.TRUE;
    }

}
