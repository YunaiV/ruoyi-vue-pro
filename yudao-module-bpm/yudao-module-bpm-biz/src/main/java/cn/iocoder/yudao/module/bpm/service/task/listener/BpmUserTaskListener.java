package cn.iocoder.yudao.module.bpm.service.task.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmListenerParamTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils.parseListenerConfig;

/**
 * BPM 用户任务通用监听器
 *
 * @author Lesan
 */
@Component
@Slf4j
public class BpmUserTaskListener implements TaskListener {

    public static final String DELEGATE_EXPRESSION = "${bpmUserTaskListener}";

    @Resource
    private BpmModelService modelService;

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Resource
    private RestTemplate restTemplate;

    @Override
    public void notify(DelegateTask delegateTask) {
        // 1. 获取所需基础信息
        HistoricProcessInstance processInstance = processInstanceService.getHistoricProcessInstance(delegateTask.getProcessInstanceId());
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(delegateTask.getProcessDefinitionId());
        UserTask userTask = (UserTask) BpmnModelUtils.getFlowElementById(bpmnModel, delegateTask.getTaskDefinitionKey());
        BpmSimpleModelNodeVO.ListenerHandler listenerHandler = getListenerHandlerByEvent(delegateTask.getEventName(),
                userTask.getTaskListeners());

        // 2. 获取请求头和请求体
        Map<String, Object> processVariables = processInstance.getProcessVariables();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        parseListenerMap(listenerHandler.getHeader(), processVariables, headers);
        parseListenerMap(listenerHandler.getBody(), processVariables, body);
        // 2.1 请求头默认参数
        if (StrUtil.isNotEmpty(delegateTask.getTenantId())) {
            headers.add(HEADER_TENANT_ID, delegateTask.getTenantId());
        }
        // 2.2 请求体默认参数
        // TODO @芋艿：哪些默认参数，后续再调研下；感觉可以搞个 task 字段，把整个 delegateTask 放进去；
        body.add("processInstanceId", delegateTask.getProcessInstanceId());
        body.add("assignee", delegateTask.getAssignee());
        body.add("taskDefinitionKey", delegateTask.getTaskDefinitionKey());
        body.add("taskId", delegateTask.getId());

        // 3. 异步发起请求
        // TODO @芋艿：确认要同步，还是异步
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        // TODO @lesan：可能需要 try catch 哇？ RestClientException
        ResponseEntity<String> responseEntity = restTemplate.exchange(listenerHandler.getPath(), HttpMethod.POST,
                requestEntity, String.class);
        log.info("[notify][监听器：{}，事件类型：{}，请求头：{}，请求体：{}，响应结果：{}]",
                DELEGATE_EXPRESSION,
                delegateTask.getEventName(),
                headers,
                body,
                responseEntity);
        // 4. 是否需要后续操作？TODO 芋艿：待定！
    }

    private void parseListenerMap(List<BpmSimpleModelNodeVO.ListenerHandler.ListenerParam> list,
                                  Map<String, Object> processVariables,
                                  MultiValueMap<String, String> to) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.forEach(item -> {
            if (item.getType().equals(BpmListenerParamTypeEnum.FIXED_VALUE.getType())) {
                to.add(item.getKey(), item.getValue());
            } else if (item.getType().equals(BpmListenerParamTypeEnum.FROM_FORM.getType())) {
                to.add(item.getKey(), processVariables.get(item.getValue()).toString());
            }
        });
    }

    private BpmSimpleModelNodeVO.ListenerHandler getListenerHandlerByEvent(String eventName, List<FlowableListener> node) {
        FlowableListener flowableListener = node.stream()
                .filter(item -> item.getEvent().equals(eventName))
                .findFirst().orElse(null);
        Assert.notNull(flowableListener, "监听器({})不能为空", flowableListener);
        return parseListenerConfig(flowableListener);
    }

}