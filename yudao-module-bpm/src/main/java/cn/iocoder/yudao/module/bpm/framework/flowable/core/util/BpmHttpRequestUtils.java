package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmHttpRequestParamTypeEnum;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_INSTANCE_HTTP_CALL_ERROR;

/**
 * 工作流发起 HTTP 请求工具类
 *
 * @author 芋道源码
 */
@Slf4j
public class BpmHttpRequestUtils {

    public static void executeBpmHttpRequest(ProcessInstance processInstance,
                                             String url,
                                             List<BpmSimpleModelNodeVO.HttpRequestParam> headerParams,
                                             List<BpmSimpleModelNodeVO.HttpRequestParam> bodyParams,
                                             Boolean handleResponse,
                                             List<KeyValue<String, String>> response) {
        BpmProcessInstanceService processInstanceService = SpringUtils.getBean(BpmProcessInstanceService.class);

        // 1.1 设置请求头
        MultiValueMap<String, String> headers = buildHttpHeaders(processInstance, headerParams);
        // 1.2 设置请求体
        MultiValueMap<String, String> body = buildHttpBody(processInstance, bodyParams);

        // 2. 发起请求
        RestTemplate restTemplate = SpringUtils.getBean(RestTemplate.class);
        ResponseEntity<String> responseEntity = sendHttpRequest(url, headers, body, restTemplate);

        // 3. 处理返回
        if (Boolean.FALSE.equals(handleResponse)) {
            return;
        }
        // 3.1 判断是否需要解析返回值
        if (responseEntity == null
                || StrUtil.isEmpty(responseEntity.getBody())
                || !responseEntity.getStatusCode().is2xxSuccessful()
                || CollUtil.isEmpty(response)) {
            return;
        }
        // 3.2 解析返回值, 返回值必须符合 CommonResult 规范。
        CommonResult<Map<String, Object>> respResult = JsonUtils.parseObjectQuietly(responseEntity.getBody(),
                new TypeReference<>() {});
        if (respResult == null || !respResult.isSuccess()) {
            return;
        }
        // 3.3 获取需要更新的流程变量
        Map<String, Object> updateVariables = getNeedUpdatedVariablesFromResponse(respResult.getData(), response);
        // 3.4 更新流程变量
        if (CollUtil.isNotEmpty(updateVariables)) {
            processInstanceService.updateProcessInstanceVariables(processInstance.getId(), updateVariables);
        }
    }

    public static void executeBpmHttpRequest(BpmProcessInstanceStatusEvent event,
                                             String url) {
        // 1.1 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (TenantContextHolder.getTenantId() != null) {
            headers.add(HEADER_TENANT_ID, String.valueOf(TenantContextHolder.getTenantId()));
        } else {
            BpmProcessInstanceService processInstanceService = SpringUtils.getBean(BpmProcessInstanceService.class);
            ProcessInstance processInstance = processInstanceService.getProcessInstance(event.getId());
            if (processInstance != null) {
                headers.add(HEADER_TENANT_ID, String.valueOf(TenantContextHolder.getTenantId()));
            }
        }
        // 1.2 设置请求体
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("id", event.getId());
//        body.add("processDefinitionKey", event.getProcessDefinitionKey());
//        body.add("status", event.getStatus().toString());
//        if (StrUtil.isNotEmpty(event.getBusinessKey())) {
//            body.add("businessKey", event.getBusinessKey());
//        }

        // 2. 发起请求
        RestTemplate restTemplate = SpringUtils.getBean(RestTemplate.class);
        sendHttpRequest(url, headers, event, restTemplate);
    }

    public static ResponseEntity<String> sendHttpRequest(String url,
                                                         MultiValueMap<String, String> headers,
                                                         Object body,
                                                         RestTemplate restTemplate) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            log.info("[sendHttpRequest][HTTP 请求，请求头：{}，请求体：{}，响应结果：{}]", headers, body, responseEntity);
        } catch (RestClientException e) {
            log.error("[sendHttpRequest][HTTP 请求，请求头：{}，请求体：{}，请求出错：{}]", headers, body, e.getMessage());
            throw exception(PROCESS_INSTANCE_HTTP_CALL_ERROR);
        }
        return responseEntity;
    }

    public static MultiValueMap<String, String> buildHttpHeaders(ProcessInstance processInstance,
                                                                 List<BpmSimpleModelNodeVO.HttpRequestParam> headerSettings) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HEADER_TENANT_ID, processInstance.getTenantId());
        Map<String, Object> processVariables = processInstance.getProcessVariables();
        addHttpRequestParam(headers, headerSettings, processVariables);
        return headers;
    }

    public static MultiValueMap<String, String> buildHttpBody(ProcessInstance processInstance,
                                                              List<BpmSimpleModelNodeVO.HttpRequestParam> bodySettings) {
        Map<String, Object> processVariables = processInstance.getProcessVariables();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        addHttpRequestParam(body, bodySettings, processVariables);
        if (!body.containsKey("processInstanceId")) { // 避免重复添加
            body.add("processInstanceId", processInstance.getId());
        }
        return body;
    }

    /**
     * 从请求返回值获取需要更新的流程变量
     *
     * @param result           请求返回结果
     * @param responseSettings 返回设置
     * @return 需要更新的流程变量
     */
    public static Map<String, Object> getNeedUpdatedVariablesFromResponse(Map<String, Object> result,
                                                                          List<KeyValue<String, String>> responseSettings) {
        Map<String, Object> updateVariables = new HashMap<>();
        if (CollUtil.isEmpty(result)) {
            return updateVariables;
        }
        responseSettings.forEach(responseSetting -> {
            if (StrUtil.isNotEmpty(responseSetting.getKey()) && result.containsKey(responseSetting.getValue())) {
                updateVariables.put(responseSetting.getKey(), result.get(responseSetting.getValue()));
            }
        });
        return updateVariables;
    }

    /**
     * 添加 HTTP 请求参数。请求头或者请求体
     *
     * @param params           HTTP 请求参数
     * @param paramSettings    HTTP 请求参数设置
     * @param processVariables 流程变量
     */
    public static void addHttpRequestParam(MultiValueMap<String, String> params,
                                           List<BpmSimpleModelNodeVO.HttpRequestParam> paramSettings,
                                           Map<String, Object> processVariables) {
        if (CollUtil.isEmpty(paramSettings)) {
            return;
        }
        paramSettings.forEach(item -> {
            if (item.getType().equals(BpmHttpRequestParamTypeEnum.FIXED_VALUE.getType())) {
                params.add(item.getKey(), item.getValue());
            } else if (item.getType().equals(BpmHttpRequestParamTypeEnum.FROM_FORM.getType())) {
                params.add(item.getKey(), processVariables.get(item.getValue()).toString());
            }
        });
    }

}
