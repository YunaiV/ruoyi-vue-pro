package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.demo.exection;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * 类型为 delegateExpression 的 ExecutionListener 监听器示例
 *
 * 和 {@link DemoDelegateClassExecutionListener} 的差异是，需要注册到 Spring 中
 */
@Component
@Slf4j
public class DemoDelegateExpressionExecutionListener implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("[execute][execution({}) 被调用！变量有：{}]", execution.getId(),
                execution.getCurrentFlowableListener().getFieldExtensions());
    }

}