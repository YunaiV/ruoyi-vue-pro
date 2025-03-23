package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * {@link IotDataBridgeExecute} 实现类的测试
 *
 * @author HUIHUI
 */
@Disabled // 默认禁用，需要手动启用测试
@Slf4j
public class IotDataBridgeExecuteTest extends BaseMockitoUnitTest {

    private IotDeviceMessage message;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private IotHttpDataBridgeExecute httpDataBridgeExecute;

    @BeforeEach
    public void setUp() {
        // 创建共享的测试消息
        message = IotDeviceMessage.builder().requestId("TEST-001").reportTime(LocalDateTime.now()).tenantId(1L)
                .productKey("testProduct").deviceName("testDevice").deviceKey("testDeviceKey")
                .type("property").identifier("temperature").data("{\"value\": 60}")
                .build();

        // 配置 RestTemplate mock 返回成功响应
        // TODO @puhui999：这个应该放到 testHttpDataBridge 里
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));
    }

    @Test
    public void testKafkaMQDataBridge() {
        // 1. 创建执行器实例
        IotKafkaMQDataBridgeExecute action = new IotKafkaMQDataBridgeExecute();

        // 2. 创建配置
        // TODO @puhui999：可以改成链式哈。
        IotDataBridgeKafkaMQConfig config = new IotDataBridgeKafkaMQConfig();
        config.setBootstrapServers("127.0.0.1:9092");
        config.setTopic("test-topic");
        config.setSsl(false);
        config.setUsername(null);
        config.setPassword(null);

        // 3. 执行两次测试，验证缓存
        log.info("[testKafkaMQDataBridge][第一次执行，应该会创建新的 producer]");
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));

        log.info("[testKafkaMQDataBridge][第二次执行，应该会复用缓存的 producer]");
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));
    }

    @Test
    public void testRabbitMQDataBridge() {
        // 1. 创建执行器实例
        IotRabbitMQDataBridgeExecute action = new IotRabbitMQDataBridgeExecute();

        // 2. 创建配置
        IotDataBridgeRabbitMQConfig config = new IotDataBridgeRabbitMQConfig();
        config.setHost("localhost");
        config.setPort(5672);
        config.setVirtualHost("/");
        config.setUsername("admin");
        config.setPassword("123456");
        config.setExchange("test-exchange");
        config.setRoutingKey("test-key");
        config.setQueue("test-queue");

        // 3. 执行两次测试，验证缓存
        log.info("[testRabbitMQDataBridge][第一次执行，应该会创建新的 producer]");
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));

        log.info("[testRabbitMQDataBridge][第二次执行，应该会复用缓存的 producer]");
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));
    }

    @Test
    public void testRedisStreamMQDataBridge() {
        // 1. 创建执行器实例
        IotRedisStreamMQDataBridgeExecute action = new IotRedisStreamMQDataBridgeExecute();

        // 2. 创建配置
        IotDataBridgeRedisStreamMQConfig config = new IotDataBridgeRedisStreamMQConfig();
        config.setHost("127.0.0.1");
        config.setPort(6379);
        config.setDatabase(0);
        config.setPassword("123456");
        config.setTopic("test-stream");

        // 3. 执行两次测试，验证缓存
        log.info("[testRedisStreamMQDataBridge][第一次执行，应该会创建新的 producer]");
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));

        log.info("[testRedisStreamMQDataBridge][第二次执行，应该会复用缓存的 producer]");
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));
    }

    @Test
    public void testRocketMQDataBridge() {
        // 1. 创建执行器实例
        IotRocketMQDataBridgeExecute action = new IotRocketMQDataBridgeExecute();

        // 2. 创建配置
        IotDataBridgeRocketMQConfig config = new IotDataBridgeRocketMQConfig();
        config.setNameServer("127.0.0.1:9876");
        config.setGroup("test-group");
        config.setTopic("test-topic");
        config.setTags("test-tag");

        // 3. 执行两次测试，验证缓存
        log.info("[testRocketMQDataBridge][第一次执行，应该会创建新的 producer]");
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));

        log.info("[testRocketMQDataBridge][第二次执行，应该会复用缓存的 producer]");
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));
    }

    @Test
    public void testHttpDataBridge() throws Exception {
        // 创建配置
        IotDataBridgeHttpConfig config = new IotDataBridgeHttpConfig();
        config.setUrl("https://doc.iocoder.cn/");
        config.setMethod(HttpMethod.GET.name());

        // 执行测试
        log.info("[testHttpDataBridge][执行HTTP数据桥接测试]");
        httpDataBridgeExecute.execute(message, new IotDataBridgeDO().setType(httpDataBridgeExecute.getType()).setConfig(config));
    }

}
