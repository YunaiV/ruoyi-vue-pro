package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkRabbitMQConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 的 {@link IotDataRuleAction} 实现类
 *
 * @author HUIHUI
 */
@ConditionalOnClass(name = "com.rabbitmq.client.Channel")
@Component
@Slf4j
public class IotRabbitMQDataRuleAction
        extends IotDataRuleCacheableAction<IotDataSinkRabbitMQConfig, Channel> {

    @Override
    public Integer getType() {
        return IotDataSinkTypeEnum.RABBITMQ.getType();
    }

    @Override
    public void execute(IotDeviceMessage message, IotDataSinkRabbitMQConfig config) throws Exception {
        try {
            // 1.1 获取或创建 Channel
            Channel channel = getProducer(config);
            // 1.2 声明交换机、队列和绑定关系
            channel.exchangeDeclare(config.getExchange(), "direct", true);
            channel.queueDeclare(config.getQueue(), true, false, false, null);
            channel.queueBind(config.getQueue(), config.getExchange(), config.getRoutingKey());

            // 2. 发送消息
            channel.basicPublish(config.getExchange(), config.getRoutingKey(), null,
                    JsonUtils.toJsonByte(message));
            log.info("[execute][message({}) config({}) 发送成功]", message, config);
        } catch (Exception e) {
            log.error("[execute][message({}) config({}) 发送失败]", message, config, e);
            throw e;
        }
    }

    @Override
    @SuppressWarnings("resource")
    protected Channel initProducer(IotDataSinkRabbitMQConfig config) throws Exception {
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
