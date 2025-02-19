package cn.iocoder.yudao.module.iot.service.rule.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneActionTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.service.rule.IotDataBridgeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * IoT 数据桥梁的 {@link IotRuleSceneAction} 实现类
 *
 * @author 芋道源码
 */
// TODO @芋艿：【优化】因为 bridge 会比较多，所以可以考虑在 rule 下，新建一个 bridge 的 package，然后定义一个 bridgehandler，它有：
//    1. input 方法、output 方法
//    2. build 方法，用于有状态的连接，例如说 mq、tcp、websocket
@Component
@Slf4j
public class IotRuleSceneDataBridgeAction implements IotRuleSceneAction {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private IotDataBridgeService dataBridgeService;

    @Override
    public void execute(IotDeviceMessage message, IotRuleSceneDO.ActionConfig config) {
        // 1.1 如果消息为空，直接返回
        if (message == null) {
            return;
        }
        // 1.2 获得数据桥梁
        Assert.notNull(config.getDataBridgeId(), "数据桥梁编号不能为空");
        IotDataBridgeDO dataBridge = dataBridgeService.getIotDataBridge(config.getDataBridgeId());
        if (dataBridge == null || dataBridge.getConfig() == null) {
            log.error("[execute][message({}) config({}) 对应的数据桥梁不存在]", message, config);
            return;
        }
        if (CommonStatusEnum.isDisable(dataBridge.getStatus())) {
            log.info("[execute][message({}) config({}) 对应的数据桥梁({}) 状态为禁用]", message, config, dataBridge);
            return;
        }

        // 2.1 执行 HTTP 请求
        if (IotDataBridgTypeEnum.HTTP.getType().equals(dataBridge.getType())) {
            executeHttp(message, (IotDataBridgeDO.HttpConfig) dataBridge.getConfig());
            return;
        }
        // 2.2 执行 RocketMQ 发送消息
        if (IotDataBridgTypeEnum.ROCKETMQ.getType().equals(dataBridge.getType())) {
            executeRocketMQ(message, (IotDataBridgeDO.RocketMQConfig) dataBridge.getConfig());
            return;
        }

        // TODO @芋艿：因为下面的，都是有状态的，所以通过 guava 缓存连接，然后通过 RemovalNotification 实现关闭。例如说，一次新建有效期是 10 分钟；
        // TODO @芋艿：mq-redis
        // TODO @芋艿：mq-数据库
        // TODO @芋艿：kafka
        // TODO @芋艿：rocketmq
        // TODO @芋艿：rabbitmq
        // TODO @芋艿：mqtt
        // TODO @芋艿：tcp
        // TODO @芋艿：websocket
    }

    @Override
    public IotRuleSceneActionTypeEnum getType() {
        return IotRuleSceneActionTypeEnum.DATA_BRIDGE;
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private void executeHttp(IotDeviceMessage message, IotDataBridgeDO.HttpConfig config) {
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

    private void executeRocketMQ(IotDeviceMessage message, IotDataBridgeDO.RocketMQConfig config) {
        // 1. 创建生产者实例，指定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer(config.getGroup());
        try {
            // 2. 设置 NameServer 地址
            producer.setNamesrvAddr(config.getNameServer());
            // 3. 启动生产者
            producer.start();
            // 4. 创建消息对象，指定Topic、Tag和消息体
            Message msg = new Message(
                    config.getTopic(),
                    config.getTags(),
                    message.toString().getBytes(RemotingHelper.DEFAULT_CHARSET)
            );

            // 5. 发送同步消息并处理结果
            SendResult sendResult = producer.send(msg);
            // 6. 处理发送结果
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                log.info("[executeRocketMQ][message({}) config({}) 发送成功，结果({})]",
                        message, config, sendResult);
            } else {
                log.error("[executeRocketMQ][message({}) config({}) 发送失败，结果({})]",
                        message, config, sendResult);
            }
        } catch (Exception e) {
            log.error("[executeRocketMQ][message({}) config({}) 发送异常]",
                    message, config, e);
        } finally {
            // 7. 关闭生产者
            producer.shutdown();
        }
    }

    public static void main(String[] args) {
        // 1. 创建 IotRuleSceneDataBridgeAction 实例
        IotRuleSceneDataBridgeAction action = new IotRuleSceneDataBridgeAction();

        // 2. 创建测试消息
        IotDeviceMessage message = IotDeviceMessage.builder()
                .requestId("TEST-001")
                .productKey("testProduct")
                .deviceName("testDevice")
                .deviceKey("testDeviceKey")
                .type("property")
                .identifier("temperature")
                .data("{\"value\": 60}")
                .reportTime(LocalDateTime.now())
                .tenantId(1L)
                .build();

        // 3. 创建 RocketMQ 配置
        IotDataBridgeDO.RocketMQConfig config = new IotDataBridgeDO.RocketMQConfig();
        config.setNameServer("127.0.0.1:9876");
        config.setGroup("test-group");
        config.setTopic("test-topic");
        config.setTags("test-tag");

        // 4. 执行测试
        action.executeRocketMQ(message, config);
    }

}
