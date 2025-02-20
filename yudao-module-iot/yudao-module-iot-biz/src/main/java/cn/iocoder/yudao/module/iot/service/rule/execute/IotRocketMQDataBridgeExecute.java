package cn.iocoder.yudao.module.iot.service.rule.execute;

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
public class IotRocketMQDataBridgeExecute implements IotDataBridgeExecute {

    @Override
    public void execute(IotDeviceMessage message, IotDataBridgeDO dataBridge) {
        // 1.1 校验数据桥接的类型 == ROCKETMQ
        if (!IotDataBridgTypeEnum.ROCKETMQ.getType().equals(dataBridge.getType())) {
            return;
        }
        // 1.2 执行 RocketMQ 发送消息
        executeRocketMQ(message, (IotDataBridgeDO.RocketMQConfig) dataBridge.getConfig());
    }

    private void executeRocketMQ(IotDeviceMessage message, IotDataBridgeDO.RocketMQConfig config) {
        // 1.1 创建生产者实例，指定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer(config.getGroup());
        // TODO @puhui999：可以考虑，基于 guava 做 cache，使用 config 作为 key，然后假个 listener 超时，销毁 producer
        try {
            // 1.2 设置 NameServer 地址
            producer.setNamesrvAddr(config.getNameServer());
            // 1.3 启动生产者
            producer.start();

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
        } catch (Exception e) {
            log.error("[executeRocketMQ][message({}) config({}) 发送异常]", message, config, e);
        } finally {
            // 3. 关闭生产者
            producer.shutdown();
        }
    }

    // TODO @芋艿：测试代码，后续清理
    public static void main(String[] args) {
        // 1. 创建 IotRocketMQDataBridgeExecute 实例
        IotRocketMQDataBridgeExecute action = new IotRocketMQDataBridgeExecute();

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
