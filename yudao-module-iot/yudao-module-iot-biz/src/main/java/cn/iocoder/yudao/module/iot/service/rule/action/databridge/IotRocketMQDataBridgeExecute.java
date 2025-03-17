package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.config.IotDataBridgeRocketMQConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgeTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 的 {@link IotDataBridgeExecute} 实现类
 *
 * @author HUIHUI
 */
@ConditionalOnClass(name = "org.apache.rocketmq.client.producer.DefaultMQProducer")
@Component
@Slf4j
public class IotRocketMQDataBridgeExecute extends
        AbstractCacheableDataBridgeExecute<IotDataBridgeRocketMQConfig, DefaultMQProducer> {

    @Override
    public Integer getType() {
        return IotDataBridgeTypeEnum.ROCKETMQ.getType();
    }

    @Override
    public void execute0(IotDeviceMessage message, IotDataBridgeRocketMQConfig config) throws Exception {
        // 1. 获取或创建 Producer
        DefaultMQProducer producer = getProducer(config);

        // 2.1 创建消息对象，指定Topic、Tag和消息体
        Message msg = new Message(
                config.getTopic(),
                config.getTags(),
                message.toString().getBytes(RemotingHelper.DEFAULT_CHARSET)
        );
        // 2.2 发送同步消息并处理结果
        SendResult sendResult = producer.send(msg);
        // 2.3 处理发送结果
        if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            log.info("[executeRocketMQ][message({}) config({}) 发送成功，结果({})]", message, config, sendResult);
        } else {
            log.error("[executeRocketMQ][message({}) config({}) 发送失败，结果({})]", message, config, sendResult);
        }
    }

    @Override
    protected DefaultMQProducer initProducer(IotDataBridgeRocketMQConfig config) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(config.getGroup());
        producer.setNamesrvAddr(config.getNameServer());
        producer.start();
        return producer;
    }

    @Override
    protected void closeProducer(DefaultMQProducer producer) {
        producer.shutdown();
    }

}
