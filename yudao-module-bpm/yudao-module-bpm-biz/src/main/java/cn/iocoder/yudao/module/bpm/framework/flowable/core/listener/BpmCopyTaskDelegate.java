package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceCopyService;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

import static cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.BpmCopyTaskDelegate.BEAN_NAME;

/**
 * 处理抄送用户的 {@link JavaDelegate} 的实现类
 * <p>
 * 目前只有仿钉钉/飞书模式的【抄送节点】使用
 *
 * @author jason
 */
@Component(BEAN_NAME)
public class BpmCopyTaskDelegate implements JavaDelegate {

    public static final String BEAN_NAME = "bpmCopyTaskDelegate";

    @Resource
    private BpmTaskCandidateInvoker taskCandidateInvoker;

    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;

    @Override
    public void execute(DelegateExecution execution) {
        // 1. 获得抄送人
        Set<Long> userIds = taskCandidateInvoker.calculateUsersByTask(execution);
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        // 2. 执行抄送
        FlowElement currentFlowElement = execution.getCurrentFlowElement();
        processInstanceCopyService.createProcessInstanceCopy(userIds, null, execution.getProcessInstanceId(),
                currentFlowElement.getId(), currentFlowElement.getName(), null);
    }

}
