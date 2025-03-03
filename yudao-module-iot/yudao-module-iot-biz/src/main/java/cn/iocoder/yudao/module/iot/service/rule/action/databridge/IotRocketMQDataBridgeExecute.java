package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * RocketMQ 的 {@link IotDataBridgeExecute} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotRocketMQDataBridgeExecute extends
        AbstractCacheableDataBridgeExecute<IotDataBridgeDO.RocketMQConfig, DefaultMQProducer> {

    @Override
    public Integer getType() {
        return IotDataBridgTypeEnum.ROCKETMQ.getType();
    }

    @Override
    public void execute0(IotDeviceMessage message, IotDataBridgeDO.RocketMQConfig config) throws Exception {
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
    protected DefaultMQProducer initProducer(IotDataBridgeDO.RocketMQConfig config) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(config.getGroup());
        producer.setNamesrvAddr(config.getNameServer());
        producer.start();
        return producer;
    }

    @Override
    protected void closeProducer(DefaultMQProducer producer) {
        producer.shutdown();
    }

    // TODO @芋艿：测试代码，后续清理
    public static void main(String[] args) {
        // 1. 创建一个共享的实例
        IotRocketMQDataBridgeExecute action = new IotRocketMQDataBridgeExecute();

        // 2. 创建共享的配置
        IotDataBridgeDO.RocketMQConfig config = new IotDataBridgeDO.RocketMQConfig();
        config.setNameServer("127.0.0.1:9876");
        config.setGroup("test-group");
        config.setTopic("test-topic");
        config.setTags("test-tag");

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
        log.info("[main][第一次执行，应该会创建新的 producer]");
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));

        log.info("[main][第二次执行，应该会复用缓存的 producer]");
        action.execute(message, new IotDataBridgeDO().setType(action.getType()).setConfig(config));
    }

}
