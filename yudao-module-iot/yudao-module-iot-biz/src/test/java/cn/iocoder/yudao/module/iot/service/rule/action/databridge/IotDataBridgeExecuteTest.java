package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataSinkDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.*;
import cn.iocoder.yudao.module.iot.service.rule.data.action.*;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * {@link IotDataRuleAction} 实现类的单元测试
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
    private IotHttpDataSinkAction httpDataBridgeExecute;

    @BeforeEach
    public void setUp() {
        // TODO @芋艿：@puhui999：需要调整下；
        // 创建共享的测试消息
        //message = IotDeviceMessage.builder().messageId("TEST-001").reportTime(LocalDateTime.now())
        //        .productKey("testProduct").deviceName("testDevice")
        //        .type("property").identifier("temperature").data("{\"value\": 60}")
        //        .build();
    }

    @Test
    public void testKafkaMQDataBridge() throws Exception {
        // 1. 创建执行器实例
        IotKafkaDataRuleAction action = new IotKafkaDataRuleAction();

        // 2. 创建配置
        IotDataSinkKafkaConfig config = new IotDataSinkKafkaConfig()
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
        IotRabbitMQDataRuleAction action = new IotRabbitMQDataRuleAction();

        // 2. 创建配置
        IotDataSinkRabbitMQConfig config = new IotDataSinkRabbitMQConfig()
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
    public void testRedisDataBridge() throws Exception {
        // 1. 创建执行器实例
        IotRedisRuleAction action = new IotRedisRuleAction();

        // 2. 创建配置 - 测试 Stream 数据结构
        IotDataSinkRedisConfig config = new IotDataSinkRedisConfig();
        config.setHost("127.0.0.1");
        config.setPort(6379);
        config.setDatabase(0);
        config.setPassword("123456");
        config.setTopic("test-stream");
        config.setDataStructure(1); // Stream 类型

        // 3. 执行测试并验证缓存
        executeAndVerifyCache(action, config, "Redis");
    }

    @Test
    public void testRocketMQDataBridge() throws Exception {
        // 1. 创建执行器实例
        IotRocketMQDataRuleAction action = new IotRocketMQDataRuleAction();

        // 2. 创建配置
        IotDataSinkRocketMQConfig config = new IotDataSinkRocketMQConfig()
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
        IotDataSinkHttpConfig config = new IotDataSinkHttpConfig()
                .setUrl("https://doc.iocoder.cn/").setMethod(HttpMethod.GET.name());

        // 3. 执行测试
        log.info("[testHttpDataBridge][执行HTTP数据桥接测试]");
        httpDataBridgeExecute.execute(message, new IotDataSinkDO()
                .setType(httpDataBridgeExecute.getType()).setConfig(config));
    }

    /**
     * 执行测试并验证缓存的通用方法
     *
     * @param action 执行器实例
     * @param config 配置对象
     * @param type MQ 类型
     * @throws Exception 如果执行过程中发生异常
     */
    private void executeAndVerifyCache(IotDataRuleAction action, IotAbstractDataSinkConfig config, String type)
            throws Exception {
        log.info("[test{}DataBridge][第一次执行，应该会创建新的 producer]", type);
        action.execute(message, new IotDataSinkDO().setType(action.getType()).setConfig(config));

        log.info("[test{}DataBridge][第二次执行，应该会复用缓存的 producer]", type);
        action.execute(message, new IotDataSinkDO().setType(action.getType()).setConfig(config));
    }

}
