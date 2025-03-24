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
 * {@link IotDataBridgeExecute} 实现类的单元测试
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
        message = IotDeviceMessage.builder()
                .requestId("TEST-001")
                .reportTime(LocalDateTime.now())
                .tenantId(1L)
                .productKey("testProduct")
                .deviceName("testDevice")
                .deviceKey("testDeviceKey")
                .type("property")
                .identifier("temperature")
                .data("{\"value\": 60}")
                .build();
    }

    @Test
    public void testKafkaMQDataBridge() throws Exception {
        // 1. 创建执行器实例
        IotKafkaMQDataBridgeExecute action = new IotKafkaMQDataBridgeExecute();

        // 2. 创建配置
        IotDataBridgeKafkaMQConfig config = new IotDataBridgeKafkaMQConfig()
                .setBootstrapServers("127.0.0.1:9092")
                .setTopic("test-topic")
                .setSsl(false)
                .setUsername(null)
                .setPassword(null);

        // 3. 执行测试并验证缓存
        executeAndVerifyCache(action, config, "KafkaMQ");
    }

    @Test
    public void testRabbitMQDataBridge() throws Exception {
        // 1. 创建执行器实例
        IotRabbitMQDataBridgeExecute action = new IotRabbitMQDataBridgeExecute();

        // 2. 创建配置
        IotDataBridgeRabbitMQConfig config = new IotDataBridgeRabbitMQConfig()
                .setHost("localhost")
                .setPort(5672)
                .setVirtualHost("/")
                .setUsername("admin")
                .setPassword("123456")
                .setExchange("test-exchange")
                .setRoutingKey("test-key")
                .setQueue("test-queue");

        // 3. 执行测试并验证缓存
        executeAndVerifyCache(action, config, "RabbitMQ");
    }

    @Test
    public void testRedisStreamMQDataBridge() throws Exception {
        // 1. 创建执行器实例
        IotRedisStreamMQDataBridgeExecute action = new IotRedisStreamMQDataBridgeExecute();

        // 2. 创建配置
        IotDataBridgeRedisMQConfig config = new IotDataBridgeRedisMQConfig()
                .setHost("127.0.0.1")
                .setPort(6379)
                .setDatabase(0)
                .setPassword("123456")
                .setTopic("test-stream");

        // 3. 执行测试并验证缓存
        executeAndVerifyCache(action, config, "RedisStreamMQ");
    }

    @Test
    public void testRocketMQDataBridge() throws Exception {
        // 1. 创建执行器实例
        IotRocketMQDataBridgeExecute action = new IotRocketMQDataBridgeExecute();

        // 2. 创建配置
        IotDataBridgeRocketMQConfig config = new IotDataBridgeRocketMQConfig()
                .setNameServer("127.0.0.1:9876")
                .setGroup("test-group")
                .setTopic("test-topic")
                .setTags("test-tag");

        // 3. 执行测试并验证缓存
        executeAndVerifyCache(action, config, "RocketMQ");
    }

    @Test
    public void testHttpDataBridge() throws Exception {
        // 1. 配置 RestTemplate mock 返回成功响应
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // 2. 创建配置
        IotDataBridgeHttpConfig config = new IotDataBridgeHttpConfig()
                .setUrl("https://doc.iocoder.cn/")
                .setMethod(HttpMethod.GET.name());

        // 3. 执行测试
        log.info("[testHttpDataBridge][执行HTTP数据桥接测试]");
        httpDataBridgeExecute.execute(message, new IotDataBridgeDO()
                .setType(httpDataBridgeExecute.getType())
                .setConfig(config));
    }

    /**
     * 执行测试并验证缓存的通用方法
     *
     * @param action 执行器实例
     * @param config 配置对象
     * @param mqType MQ类型
     * @throws Exception 如果执行过程中发生异常
     */
    private void executeAndVerifyCache(IotDataBridgeExecute<?> action, IotDataBridgeAbstractConfig config, String mqType) throws Exception {
        log.info("[test{}DataBridge][第一次执行，应该会创建新的 producer]", mqType);
        action.execute(message, new IotDataBridgeDO()
                .setType(action.getType())
                .setConfig(config));

        log.info("[test{}DataBridge][第二次执行，应该会复用缓存的 producer]", mqType);
        action.execute(message, new IotDataBridgeDO()
                .setType(action.getType())
                .setConfig(config));
    }

}
