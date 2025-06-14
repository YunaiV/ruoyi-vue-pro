package cn.iocoder.yudao.module.iot.core.messagebus.core.local;

import cn.iocoder.yudao.module.iot.core.messagebus.config.IotMessageBusAutoConfiguration;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotLocalMessageBus} 集成测试
 *
 * @author 芋道源码
 */
@SpringBootTest(classes = LocalIotMessageBusIntegrationTest.class)
@Import(IotMessageBusAutoConfiguration.class)
@TestPropertySource(properties = {
    "yudao.iot.message-bus.type=local"
})
@Slf4j
public class LocalIotMessageBusIntegrationTest {

    @Resource
    private IotMessageBus messageBus;

    /**
     * 1 topic 2 subscriber
     */
    @Test
    public void testSendMessageWithTwoSubscribers() throws InterruptedException {
        // 准备
        String topic = "test-topic";
        String testMessage = "Hello IoT Message Bus!";
        // 用于等待消息处理完成
        CountDownLatch latch = new CountDownLatch(2);
        // 用于记录接收到的消息
        AtomicInteger subscriber1Count = new AtomicInteger(0);
        AtomicInteger subscriber2Count = new AtomicInteger(0);

        // 创建第一个订阅者
        IotMessageSubscriber<String> subscriber1 = new IotMessageSubscriber<>() {

            @Override
            public String getTopic() {
                return topic;
            }

            @Override
            public String getGroup() {
                return "group1";
            }

            @Override
            public void onMessage(String message) {
                log.info("[订阅者1] 收到消息 - Topic: {}, Message: {}", getTopic(), message);
                subscriber1Count.incrementAndGet();
                assertEquals(testMessage, message);
                latch.countDown();
            }

        };
        // 创建第二个订阅者
        IotMessageSubscriber<String> subscriber2 = new IotMessageSubscriber<>() {

            @Override
            public String getTopic() {
                return topic;
            }

            @Override
            public String getGroup() {
                return "group2";
            }

            @Override
            public void onMessage(String message) {
                log.info("[订阅者2] 收到消息 - Topic: {}, Message: {}", getTopic(), message);
                subscriber2Count.incrementAndGet();
                assertEquals(testMessage, message);
                latch.countDown();
            }

        };
        // 注册订阅者
        messageBus.register(subscriber1);
        messageBus.register(subscriber2);

        // 发送消息
        log.info("[测试] 发送消息 - Topic: {}, Message: {}", topic, testMessage);
        messageBus.post(topic, testMessage);
        // 等待消息处理完成（最多等待 10 秒）
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // 验证结果
        assertTrue(completed, "消息处理超时");
        assertEquals(1, subscriber1Count.get(), "订阅者 1 应该收到 1 条消息");
        assertEquals(1, subscriber2Count.get(), "订阅者 2 应该收到 1 条消息");
        log.info("[测试] 测试完成 - 订阅者 1 收到{}条消息，订阅者 2 收到{}条消息", subscriber1Count.get(), subscriber2Count.get());
    }

    /**
     * 2 topic 2 subscriber
     */
    @Test
    public void testMultipleTopics() throws InterruptedException {
        // 准备
        String topic1 = "device-status";
        String topic2 = "device-data";
        String message1 = "设备在线";
        String message2 = "温度:25°C";
        CountDownLatch latch = new CountDownLatch(2);

        // 创建订阅者 1 - 只订阅设备状态
        IotMessageSubscriber<String> statusSubscriber = new IotMessageSubscriber<>() {

            @Override
            public String getTopic() {
                return topic1;
            }

            @Override
            public String getGroup() {
                return "status-group";
            }

            @Override
            public void onMessage(String message) {
                log.info("[状态订阅者] 收到消息 - Topic: {}, Message: {}", getTopic(), message);
                assertEquals(message1, message);
                latch.countDown();
            }

        };
        // 创建订阅者 2 - 只订阅设备数据
        IotMessageSubscriber<String> dataSubscriber = new IotMessageSubscriber<>() {

            @Override
            public String getTopic() {
                return topic2;
            }

            @Override
            public String getGroup() {
                return "data-group";
            }

            @Override
            public void onMessage(String message) {
                log.info("[数据订阅者] 收到消息 - Topic: {}, Message: {}", getTopic(), message);
                assertEquals(message2, message);
                latch.countDown();
            }

        };
        // 注册订阅者到不同主题
        messageBus.register(statusSubscriber);
        messageBus.register(dataSubscriber);

        // 发送消息到不同主题
        messageBus.post(topic1, message1);
        messageBus.post(topic2, message2);
        // 等待消息处理完成
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "消息处理超时");
        log.info("[测试] 多主题测试完成");
    }

}