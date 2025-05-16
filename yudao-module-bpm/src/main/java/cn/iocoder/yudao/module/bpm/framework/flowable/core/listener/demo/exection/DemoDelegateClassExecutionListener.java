package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.demo.exection;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * 类型为 class 的 ExecutionListener 监听器示例
 *
 * @author 芋道源码
 */
@Slf4j
public class DemoDelegateClassExecutionListener implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("[execute][execution({}) 被调用！变量有：{}]", execution.getId(),
                execution.getCurrentFlowableListener().getFieldExtensions());
    }

}