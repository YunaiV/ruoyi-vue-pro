package cn.iocoder.yudao.module.bpm.service.task.listener;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmListenerMapType;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils.parseExtensionElement;

/**
 * BPM 用户任务通用监听器
 *
 * @author Lesan
 */
@Component
@Slf4j
public class BpmUserTaskListener implements TaskListener {

    public static final String DELEGATE_EXPRESSION = "${bpmUserTaskListener}";

    public static final String EXTENSION_SUFFIX = "TaskListenerMetaInfo";

    @Resource
    private BpmModelService modelService;

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public void notify(DelegateTask delegateTask) {
        // 1. 获取所需基础信息
        HistoricProcessInstance processInstance = processInstanceService.getHistoricProcessInstance(delegateTask.getProcessInstanceId());
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(delegateTask.getProcessDefinitionId());
        FlowElement userTaskElement = BpmnModelUtils.getFlowElementById(bpmnModel, delegateTask.getTaskDefinitionKey());
        // TODO @lesan：可以写到 FlowableUtils 里，简化解析逻辑！
        BpmSimpleModelNodeVO.ListenerHandler listenerHandler = JSONUtil.toBean(
                parseExtensionElement(userTaskElement, delegateTask.getEventName() + EXTENSION_SUFFIX),
                BpmSimpleModelNodeVO.ListenerHandler.class);

        // 2. 获取请求头和请求体
        Map<String, Object> processVariables = processInstance.getProcessVariables();
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        listenerHandler.getHeader().forEach(item -> {
            // TODO @lesan：可以写个统一的方法，解析参数。然后非空，put 到 headers 或者 body 里！
            if (item.getType().equals(BpmListenerMapType.FIXED_VALUE.getType())) {
                headers.put(item.getKey(), item.getValue());
            } else if (item.getType().equals(BpmListenerMapType.FROM_FORM.getType())) {
                headers.put(item.getKey(), processVariables.getOrDefault(item.getValue(), "").toString());
            }
        });
        // TODO @lesan：header 里面，需要添加下 tenant-id！
        listenerHandler.getBody().forEach(item -> {
            if (item.getType().equals(BpmListenerMapType.FIXED_VALUE.getType())) {
                body.put(item.getKey(), item.getValue());
            } else if (item.getType().equals(BpmListenerMapType.FROM_FORM.getType())) {
                body.put(item.getKey(), processVariables.getOrDefault(item.getValue(), ""));
            }
        });

        // 3. 异步发起请求
        // TODO @lesan：最好打印下日志！
        HttpRequest.post(listenerHandler.getPath())
                .addHeaders(headers).form(body).executeAsync();

        // 4. 是否需要后续操作？TODO 芋艿：待定！
    }

}