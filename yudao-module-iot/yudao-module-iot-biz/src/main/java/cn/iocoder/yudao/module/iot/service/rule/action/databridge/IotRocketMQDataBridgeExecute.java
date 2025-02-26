package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataBridgTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;

/**
 * RocketMQ 的 {@link IotDataBridgeExecute} 实现类
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotRocketMQDataBridgeExecute implements IotDataBridgeExecute {

    /**
     * 针对 {@link IotDataBridgeDO.RocketMQConfig} 的 DefaultMQProducer 缓存
     */
    // TODO @puhui999：因为 kafka 之类也存在这个情况，是不是得搞个抽象类。提供一个 initProducer，和 closeProducer 方法
    private final LoadingCache<IotDataBridgeDO.RocketMQConfig, DefaultMQProducer> PRODUCER_CACHE = CacheBuilder.newBuilder()
            .refreshAfterWrite(Duration.ofMinutes(10)) // TODO puhui999：应该是 read 30 分钟哈
            // 增加移除监听器，自动关闭 producer
            .removalListener(notification -> {
                DefaultMQProducer producer = (DefaultMQProducer) notification.getValue();
                // TODO puhui999：if return，更简短哈
                if (producer != null) {
                    try {
                        producer.shutdown();
                        log.info("[PRODUCER_CACHE][配置({}) 对应的 producer 已关闭]", notification.getKey());
                    } catch (Exception e) {
                        log.error("[PRODUCER_CACHE][配置({}) 对应的 producer 关闭失败]", notification.getKey(), e);
                    }
                }
            })
            // TODO @puhui999：就同步哈，不用异步处理。
            // 通过 asyncReloading 实现全异步加载，包括 refreshAfterWrite 被阻塞的加载线程
            .build(CacheLoader.asyncReloading(new CacheLoader<IotDataBridgeDO.RocketMQConfig, DefaultMQProducer>() {

                @Override
                public DefaultMQProducer load(IotDataBridgeDO.RocketMQConfig config) throws Exception {
                    DefaultMQProducer producer = new DefaultMQProducer(config.getGroup());
                    producer.setNamesrvAddr(config.getNameServer());
                    producer.start();
                    log.info("[PRODUCER_CACHE][配置({}) 对应的 producer 已创建并启动]", config);
                    return producer;
                }

            }, Executors.newCachedThreadPool()));

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
        try {
            // 1. 获取或创建 Producer
            DefaultMQProducer producer = PRODUCER_CACHE.get(config);

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
        }
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
        action.executeRocketMQ(message, config);

        log.info("[main][第二次执行，应该会复用缓存的 producer]");
        action.executeRocketMQ(message, config);
    }

}
