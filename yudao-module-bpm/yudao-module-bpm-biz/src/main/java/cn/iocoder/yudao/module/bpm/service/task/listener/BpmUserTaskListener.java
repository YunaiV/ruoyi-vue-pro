package cn.iocoder.yudao.module.bpm.service.task.listener;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.el.FixedValue;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils.parseListenerConfig;

// TODO @芋艿：可能会想换个包地址
/**
 * BPM 用户任务通用监听器
 *
 * @author Lesan
 */
@Component
@Slf4j
@Scope("prototype")
public class BpmUserTaskListener implements TaskListener {

    public static final String DELEGATE_EXPRESSION = "${bpmUserTaskListener}";

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Resource
    private RestTemplate restTemplate;

    @Setter
    private FixedValue listenerConfig;

    @Override
    public void notify(DelegateTask delegateTask) {
        // 1. 获取所需基础信息
        HistoricProcessInstance processInstance = processInstanceService.getHistoricProcessInstance(delegateTask.getProcessInstanceId());
        BpmSimpleModelNodeVO.ListenerHandler listenerHandler = parseListenerConfig(listenerConfig);

        // 2. 获取请求头和请求体
        Map<String, Object> processVariables = processInstance.getProcessVariables();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        SimpleModelUtils.addHttpRequestParam(headers, listenerHandler.getHeader(), processVariables);
        SimpleModelUtils.addHttpRequestParam(body, listenerHandler.getBody(), processVariables);
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
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(listenerHandler.getPath(), HttpMethod.POST,
                    requestEntity, String.class);
            log.info("[notify][监听器：{}，事件类型：{}，请求头：{}，请求体：{}，响应结果：{}]",
                    DELEGATE_EXPRESSION,
                    delegateTask.getEventName(),
                    headers,
                    body,
                    responseEntity);
        } catch (RestClientException e) {
            log.error("[error][监听器：{}，事件类型：{}，请求头：{}，请求体：{}，请求出错：{}]",
                    DELEGATE_EXPRESSION,
                    delegateTask.getEventName(),
                    headers,
                    body,
                    e.getMessage());
        }
        // 4. 是否需要后续操作？TODO 芋艿：待定！
    }
}