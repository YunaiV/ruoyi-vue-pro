package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config.IotDataBridgeHttpConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgeTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * Http 的 {@link IotDataBridgeExecute} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotHttpDataBridgeExecute implements IotDataBridgeExecute<IotDataBridgeHttpConfig> {

    @Resource
    private RestTemplate restTemplate;

    @Override
    public Integer getType() {
        return IotDataBridgeTypeEnum.HTTP.getType();
    }

    @Override
    @SuppressWarnings({"unchecked", "deprecation"})
    public void execute0(IotDeviceMessage message, IotDataBridgeHttpConfig config) {
        String url = null;
        HttpMethod method = HttpMethod.valueOf(config.getMethod().toUpperCase());
        HttpEntity<String> requestEntity = null;
        ResponseEntity<String> responseEntity = null;
        try {
            // 1.1 构建 Header
            HttpHeaders headers = new HttpHeaders();
            if (CollUtil.isNotEmpty(config.getHeaders())) {
                config.getHeaders().putAll(config.getHeaders());
            }
            headers.add(HEADER_TENANT_ID, message.getTenantId().toString());
            // 1.2 构建 URL
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(config.getUrl());
            if (CollUtil.isNotEmpty(config.getQuery())) {
                config.getQuery().forEach(uriBuilder::queryParam);
            }
            // 1.3 构建请求体
            if (method == HttpMethod.GET) {
                uriBuilder.queryParam("message", HttpUtils.encodeUtf8(JsonUtils.toJsonString(message)));
                url = uriBuilder.build().toUriString();
                requestEntity = new HttpEntity<>(headers);
            } else {
                url = uriBuilder.build().toUriString();
                Map<String, Object> requestBody = JsonUtils.parseObject(config.getBody(), Map.class);
                if (requestBody == null) {
                    requestBody = new HashMap<>();
                }
                requestBody.put("message", message);
                headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
                requestEntity = new HttpEntity<>(JsonUtils.toJsonString(requestBody), headers);
            }

            // 2.1 发送请求
            responseEntity = restTemplate.exchange(url, method, requestEntity, String.class);
            // 2.2 记录日志
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("[executeHttp][message({}) config({}) url({}) method({}) requestEntity({}) 请求成功({})]",
                        message, config, url, method, requestEntity, responseEntity);
            } else {
                log.error("[executeHttp][message({}) config({}) url({}) method({}) requestEntity({}) 请求失败({})]",
                        message, config, url, method, requestEntity, responseEntity);
            }
        } catch (Exception e) {
            log.error("[executeHttp][message({}) config({}) url({}) method({}) requestEntity({}) 请求异常({})]",
                    message, config, url, method, requestEntity, responseEntity, e);
        }
    }

}