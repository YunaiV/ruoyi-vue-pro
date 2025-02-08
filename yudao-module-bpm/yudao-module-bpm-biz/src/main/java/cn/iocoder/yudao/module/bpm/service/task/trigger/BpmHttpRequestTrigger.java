package cn.iocoder.yudao.module.bpm.service.task.trigger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO.TriggerSetting.HttpRequestTriggerSetting;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTriggerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.util.HashMap;
import java.util.List;
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
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.exchange(setting.getUrl(), HttpMethod.POST,
                    requestEntity, String.class);
            log.info("[execute][HTTP 触发器，请求头：{}，请求体：{}，响应结果：{}]", headers, body, responseEntity);
        } catch (RestClientException e) {
            log.error("[execute][HTTP 触发器，请求头：{}，请求体：{}，请求出错：{}]", headers, body, e.getMessage());
            return;
        }

        // 4.1 判断是否需要解析返回值
        if (StrUtil.isEmpty(responseEntity.getBody())
                || !responseEntity.getStatusCode().is2xxSuccessful()
                || CollUtil.isEmpty(setting.getResponse())) {
            return;
        }
        // 4.2 解析返回值, 返回值必须符合 CommonResult 规范。
        CommonResult<Map<String, Object>> respResult = JsonUtils.parseObjectQuietly(
                responseEntity.getBody(), new TypeReference<>() {});
        if (respResult == null || !respResult.isSuccess()){
            return;
        }
        // 4.3 获取需要更新的流程变量
        Map<String, Object> updateVariables = getNeedUpdatedVariablesFromResponse(respResult.getData(), setting.getResponse());
        // 4.4 更新流程变量
        if (CollUtil.isNotEmpty(updateVariables)) {
            processInstanceService.updateProcessInstanceVariables(processInstanceId, updateVariables);
        }
    }

    /**
     * 从请求返回值获取需要更新的流程变量
     *
     * @param result 请求返回结果
     * @param responseSettings 返回设置
     * @return 需要更新的流程变量
     */
    private Map<String, Object> getNeedUpdatedVariablesFromResponse(Map<String,Object> result,
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

}
