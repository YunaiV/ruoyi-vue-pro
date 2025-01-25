package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener;

import cn.iocoder.yudao.module.bpm.enums.definition.BpmTriggerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.trigger.BpmTrigger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;

import static cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.BpmTriggerTaskDelegate.BEAN_NAME;


/**
 * 处理触发器任务 {@link JavaDelegate} 的实现类
 * <p>
 * 目前只有 Simple 设计器【触发器节点】使用
 *
 * @author jason
 */
@Component(BEAN_NAME)
@Slf4j
public class BpmTriggerTaskDelegate implements JavaDelegate {

    public static final String BEAN_NAME = "bpmTriggerTaskDelegate";

    @Resource
    private List<BpmTrigger> triggers;

    private final EnumMap<BpmTriggerTypeEnum, BpmTrigger> triggerMap = new EnumMap<>(BpmTriggerTypeEnum.class);

    @PostConstruct
    private void init() {
        triggers.forEach(trigger -> triggerMap.put(trigger.getType(), trigger));
    }

    @Override
    public void execute(DelegateExecution execution) {
        FlowElement flowElement = execution.getCurrentFlowElement();
        BpmTriggerTypeEnum bpmTriggerType = BpmnModelUtils.parserTriggerType(flowElement);
        BpmTrigger bpmTrigger = triggerMap.get(bpmTriggerType);
        if (bpmTrigger == null) {
            log.error("[execute][FlowElement({}), {} 找不到匹配的触发器]", execution.getCurrentActivityId(), flowElement);
            return;
        }
        bpmTrigger.execute(execution.getProcessInstanceId(), BpmnModelUtils.parserTriggerParam(flowElement));
    }

}
