package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config.IotDataBridgeRabbitMQConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgeTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * RabbitMQ 的 {@link IotDataBridgeExecute} 实现类
 *
 * @author HUIHUI
 */
@ConditionalOnClass(name = "com.rabbitmq.client.Channel")
@Component
@Slf4j
public class IotRabbitMQDataBridgeExecute extends
        AbstractCacheableDataBridgeExecute<IotDataBridgeRabbitMQConfig, Channel> {


    @Override
    public Integer getType() {
        return IotDataBridgeTypeEnum.RABBITMQ.getType();
    }

    @Override
    public void execute0(IotDeviceMessage message, IotDataBridgeRabbitMQConfig config) throws Exception {
        // 1. 获取或创建 Channel
        Channel channel = getProducer(config);

        // 2.1 声明交换机、队列和绑定关系
        channel.exchangeDeclare(config.getExchange(), "direct", true);
        channel.queueDeclare(config.getQueue(), true, false, false, null);
        channel.queueBind(config.getQueue(), config.getExchange(), config.getRoutingKey());

        // 2.2 发送消息
        channel.basicPublish(config.getExchange(), config.getRoutingKey(), null,
                message.toString().getBytes(StandardCharsets.UTF_8));
        log.info("[executeRabbitMQ][message({}) config({}) 发送成功]", message, config);
    }

    @Override
    @SuppressWarnings("resource")
    protected Channel initProducer(IotDataBridgeRabbitMQConfig config) throws Exception {
        // 1. 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.getHost());
        factory.setPort(config.getPort());
        factory.setVirtualHost(config.getVirtualHost());
        factory.setUsername(config.getUsername());
        factory.setPassword(config.getPassword());

        // 2. 创建连接
        Connection connection = factory.newConnection();

        // 3. 创建信道
        return connection.createChannel();
    }

    @Override
    protected void closeProducer(Channel channel) throws Exception {
        if (channel.isOpen()) {
            channel.close();
        }
        Connection connection = channel.getConnection();
        if (connection.isOpen()) {
            connection.close();
        }
    }

}
