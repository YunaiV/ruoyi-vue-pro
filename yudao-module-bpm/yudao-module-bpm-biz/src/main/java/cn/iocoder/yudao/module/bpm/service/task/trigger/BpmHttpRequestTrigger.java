package cn.iocoder.yudao.module.bpm.service.task.trigger;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO.TriggerSetting.HttpRequestTriggerSetting;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTriggerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
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

/**
 * BPM 发送 HTTP 请求触发器
 *
 * @author jason
 */
@Component
@Slf4j
public class BpmHttpRequestTrigger implements BpmTrigger {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Resource
    private RestTemplate restTemplate;

    @Override
    public BpmTriggerTypeEnum getType() {
        return BpmTriggerTypeEnum.HTTP_REQUEST;
    }

    @Override
    public void execute(String processInstanceId, String param) {
        // 1. 解析 http 请求配置
        HttpRequestTriggerSetting setting = JsonUtils.parseObject(param, HttpRequestTriggerSetting.class);
        if (setting == null) {
            log.error("[execute][流程({}) HTTP 触发器请求配置为空]", processInstanceId);
            return;
        }
        // 2.1 设置请求头
        ProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        Map<String, Object> processVariables = processInstance.getProcessVariables();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HEADER_TENANT_ID, processInstance.getTenantId());
        SimpleModelUtils.addHttpRequestParam(headers, setting.getHeader(), processVariables);
        // 2.2 设置请求体
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        SimpleModelUtils.addHttpRequestParam(body, setting.getBody(), processVariables);
        body.add("processInstanceId", processInstanceId);

        // TODO @芋艿：要不要抽象一个 Http 请求的工具类，方便复用呢？
        // 3. 发起请求
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(setting.getUrl(), HttpMethod.POST,
                    requestEntity, String.class);
            log.info("[execute][HTTP 触发器，请求头：{}，请求体：{}，响应结果：{}]", headers, body, responseEntity);
        } catch (RestClientException e) {
            log.error("[execute][HTTP 触发器，请求头：{}，请求体：{}，请求出错：{}]", headers, body, e.getMessage());
        }
    }

}
