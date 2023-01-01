package cn.iocoder.yudao.module.bpm.framework.bpm.core.event;

import cn.hutool.core.util.StrUtil;
import org.springframework.context.ApplicationListener;

/**
 * {@link BpmProcessInstanceResultEvent} 的监听器
 *
 * @author 芋道源码
 */
public abstract class BpmProcessInstanceResultEventListener
        implements ApplicationListener<BpmProcessInstanceResultEvent> {

    @Override
    public final void onApplicationEvent(BpmProcessInstanceResultEvent event) {
        if (!StrUtil.equals(event.getProcessDefinitionKey(), getProcessDefinitionKey())) {
            return;
        }
        onEvent(event);
    }

    /**
     * @return 返回监听的流程定义 Key
     */
    protected abstract String getProcessDefinitionKey();

    /**
     * 处理事件
     *
     * @param event 事件
     */
    protected abstract void onEvent(BpmProcessInstanceResultEvent event);

}
