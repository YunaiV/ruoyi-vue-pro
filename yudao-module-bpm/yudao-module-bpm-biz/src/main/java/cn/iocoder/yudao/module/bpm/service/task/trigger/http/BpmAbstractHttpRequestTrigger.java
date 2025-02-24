package cn.iocoder.yudao.module.bpm.service.task.trigger.http;

import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.trigger.BpmTrigger;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_INSTANCE_HTTP_TRIGGER_CALL_ERROR;

/**
 * BPM 发送 HTTP 请求触发器抽象类
 *
 * @author jason
 */
@Slf4j
public abstract class BpmAbstractHttpRequestTrigger implements BpmTrigger {

    @Resource
    private RestTemplate restTemplate;

    protected ResponseEntity<String> sendHttpRequest(String url,
                                                     MultiValueMap<String, String> headers,
                                                     MultiValueMap<String, String> body) {
        // TODO @芋艿：要不要抽象一个 Http 请求的工具类，方便复用呢？
        // 3. 发起请求
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            log.info("[sendHttpRequest][HTTP 触发器，请求头：{}，请求体：{}，响应结果：{}]", headers, body, responseEntity);
        } catch (RestClientException e) {
            log.error("[sendHttpRequest][HTTP 触发器，请求头：{}，请求体：{}，请求出错：{}]", headers, body, e.getMessage());
            throw exception(PROCESS_INSTANCE_HTTP_TRIGGER_CALL_ERROR);
        }
        return responseEntity;
    }

    protected MultiValueMap<String, String> buildHttpHeaders(ProcessInstance processInstance,
                                                             List<BpmSimpleModelNodeVO.HttpRequestParam> headerSettings) {
        Map<String, Object> processVariables = processInstance.getProcessVariables();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HEADER_TENANT_ID, processInstance.getTenantId());
        SimpleModelUtils.addHttpRequestParam(headers, headerSettings, processVariables);
        return headers;
    }

    protected MultiValueMap<String, String> buildHttpBody(ProcessInstance processInstance,
                                                             List<BpmSimpleModelNodeVO.HttpRequestParam> bodySettings) {
        Map<String, Object> processVariables = processInstance.getProcessVariables();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        SimpleModelUtils.addHttpRequestParam(body, bodySettings, processVariables);
        body.add("processInstanceId", processInstance.getId());
        return body;
    }

}
