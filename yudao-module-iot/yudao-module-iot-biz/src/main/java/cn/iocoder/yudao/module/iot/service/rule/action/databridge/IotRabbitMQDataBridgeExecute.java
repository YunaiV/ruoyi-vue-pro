package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * RabbitMQ 的 {@link IotDataBridgeExecute} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotRabbitMQDataBridgeExecute extends AbstractCacheableDataBridgeExecute {

    @Override
    public void execute(IotDeviceMessage message, IotDataBridgeDO dataBridge) {
        // 1.1 校验数据桥梁的类型 == RABBITMQ
        if (!IotDataBridgTypeEnum.RABBITMQ.getType().equals(dataBridge.getType())) {
            return;
        }
        // 1.2 执行 RabbitMQ 发送消息
        executeRabbitMQ(message, (IotDataBridgeDO.RabbitMQConfig) dataBridge.getConfig());
    }

    private void executeRabbitMQ(IotDeviceMessage message, IotDataBridgeDO.RabbitMQConfig config) {
        try {
            // 1. 获取或创建 Channel
            Channel channel = (Channel) getProducer(config);

            // 2.1 声明交换机、队列和绑定关系
            channel.exchangeDeclare(config.getExchange(), "direct", true);
            channel.queueDeclare(config.getQueue(), true, false, false, null);
            channel.queueBind(config.getQueue(), config.getExchange(), config.getRoutingKey());

            // 2.2 发送消息
            channel.basicPublish(config.getExchange(), config.getRoutingKey(), null,
                    message.toString().getBytes(StandardCharsets.UTF_8));
            log.info("[executeRabbitMQ][message({}) config({}) 发送成功]", message, config);
        } catch (Exception e) {
            log.error("[executeRabbitMQ][message({}) config({}) 发送异常]", message, config, e);
        }
    }

    @Override
    protected Object initProducer(Object config) throws Exception {
        IotDataBridgeDO.RabbitMQConfig rabbitConfig = (IotDataBridgeDO.RabbitMQConfig) config;

        // 1. 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitConfig.getHost());
        factory.setPort(rabbitConfig.getPort());
        factory.setVirtualHost(rabbitConfig.getVirtualHost());
        factory.setUsername(rabbitConfig.getUsername());
        factory.setPassword(rabbitConfig.getPassword());

        // 2. 创建连接
        Connection connection = factory.newConnection();

        // 3. 创建信道
        return connection.createChannel();
    }

    @Override
    protected void closeProducer(Object producer) {
        if (producer instanceof Channel) {
            try {
                Channel channel = (Channel) producer;
                if (channel.isOpen()) {
                    channel.close();
                }
                Connection connection = channel.getConnection();
                if (connection.isOpen()) {
                    connection.close();
                }
            } catch (Exception e) {
                log.error("[closeProducer][关闭 RabbitMQ 连接异常]", e);
            }
        }
    }

    // TODO @芋艿：测试代码，后续清理
    public static void main(String[] args) {
        // 1. 创建一个共享的实例
        IotRabbitMQDataBridgeExecute action = new IotRabbitMQDataBridgeExecute();

        // 2. 创建共享的配置
        IotDataBridgeDO.RabbitMQConfig config = new IotDataBridgeDO.RabbitMQConfig();
        config.setHost("localhost");
        config.setPort(5672);
        config.setVirtualHost("/");
        config.setUsername("admin");
        config.setPassword("123456");
        config.setExchange("test-exchange");
        config.setRoutingKey("test-key");
        config.setQueue("test-queue");

        // 3. 创建共享的消息
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

        // 4. 执行两次测试，验证缓存
        log.info("[main][第一次执行，应该会创建新的 channel]");
        action.executeRabbitMQ(message, config);

        log.info("[main][第二次执行，应该会复用缓存的 channel]");
        action.executeRabbitMQ(message, config);
    }
}
