package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataSinkDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkHttpConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * HTTP 的 {@link IotDataRuleAction} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotHttpDataSinkAction implements IotDataRuleAction {

    /**
     * 连接超时时间
     *
     * 设备状态消息会在事务内执行数据流转（持有设备行锁和数据库连接），所以不能使用无超时的共享 RestTemplate
     */
    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    /**
     * 读取超时时间
     *
     * 一般 Webhook 秒级即可响应；过长会拉长状态事务，过短容易误判慢接口，所以取 5 秒
     */
    static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    @Resource
    private RestTemplateBuilder restTemplateBuilder;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        // 基于共享的 RestTemplateBuilder 构建私有实例，不影响其它模块使用的 RestTemplate
        restTemplate = restTemplateBuilder.connectTimeout(CONNECT_TIMEOUT).readTimeout(READ_TIMEOUT).build();
    }

    @Override
    public Integer getType() {
        return IotDataSinkTypeEnum.HTTP.getType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute(IotDeviceMessage message, IotDataSinkDO dataSink) {
        IotDataSinkHttpConfig config = (IotDataSinkHttpConfig) dataSink.getConfig();
        Assert.notNull(config, "配置({})不能为空", dataSink.getId());
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
                headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                requestEntity = new HttpEntity<>(JsonUtils.toJsonString(requestBody), headers);
            }

            // 2. 发送请求
            responseEntity = restTemplate.exchange(url, method, requestEntity, String.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("[execute][message({}) config({}) url({}) method({}) requestEntity({}) 请求成功({})]",
                        message, config, url, method, requestEntity, responseEntity);
            } else {
                log.error("[execute][message({}) config({}) url({}) method({}) requestEntity({}) 请求失败({})]",
                        message, config, url, method, requestEntity, responseEntity);
            }
        } catch (Exception e) {
            log.error("[execute][message({}) config({}) url({}) method({}) requestEntity({}) 请求异常({})]",
                    message, config, url, method, requestEntity, responseEntity, e);
        }
    }

}
