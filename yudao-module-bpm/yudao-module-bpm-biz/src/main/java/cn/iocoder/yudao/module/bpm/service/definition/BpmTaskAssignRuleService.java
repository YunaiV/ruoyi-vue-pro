package cn.iocoder.yudao.module.bpm.service.definition;

import org.flowable.engine.delegate.DelegateExecution;

import java.util.Set;

/**
 * BPM 任务分配规则 Service 接口
 *
 * @author 芋道源码
 */
public interface BpmTaskAssignRuleService {

    /**
     * 校验流程模型的任务分配规则全部都配置了
     * 目的：如果有规则未配置，会导致流程任务找不到负责人，进而流程无法进行下去！
     *
     * @param bpmnBytes BPMN XML
     */
    void checkTaskAssignRuleAllConfig(byte[] bpmnBytes);

    /**
     * 计算当前执行任务的处理人
     *
     * @param execution 执行任务
     * @return 处理人的编号数组
     */
    Set<Long> calculateTaskCandidateUsers(DelegateExecution execution);

}
