package cn.iocoder.yudao.module.iot.core.messagebus.core.rocketmq;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.iot.core.messagebus.config.IotMessageBusAutoConfiguration;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.messagebus.core.TestMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotRocketMQMessageBus} 集成测试
 *
 * @author 芋道源码
 */
@SpringBootTest(classes = RocketMQIotMessageBusTest.class)
@Import({RocketMQAutoConfiguration.class, IotMessageBusAutoConfiguration.class})
@TestPropertySource(properties = {
    "yudao.iot.message-bus.type=rocketmq",
    "rocketmq.name-server=127.0.0.1:9876",
    "rocketmq.producer.group=test-rocketmq-group",
    "rocketmq.producer.send-message-timeout=10000"
})
@Slf4j
public class RocketMQIotMessageBusTest {

    @Resource
    private IotMessageBus messageBus;

    /**
     * 1 topic 1 subscriber（string）
     */
    @Test
    public void testSendMessageWithOneSubscriber() throws InterruptedException {
        // 准备
        String topic = "test-topic-" + IdUtil.simpleUUID();
//        String topic = "test-topic-pojo";
        String testMessage = "Hello IoT Message Bus!";
        // 用于等待消息处理完成
        CountDownLatch latch = new CountDownLatch(1);
        // 用于记录接收到的消息
        AtomicInteger subscriberCount = new AtomicInteger(0);
        AtomicReference<String> subscriberMessageRef = new AtomicReference<>();

        // 发送消息（需要提前发，保证 RocketMQ 路由的创建）
        log.info("[测试] 发送消息 - Topic: {}, Message: {}", topic, testMessage);
        messageBus.post(topic, testMessage);

        // 创建订阅者
        IotMessageSubscriber<String> subscriber1 = new IotMessageSubscriber<>() {

            @Override
            public String getTopic() {
                return topic;
            }

            @Override
            public String getGroup() {
                return "test-topic-" + IdUtil.simpleUUID() + "-consumer";
//                return "test-topic-consumer-01";
            }

            @Override
            public void onMessage(String message) {
                log.info("[订阅者] 收到消息 - Topic: {}, Message: {}", getTopic(), message);
                subscriberCount.incrementAndGet();
                subscriberMessageRef.set(message);
                assertEquals(testMessage, message);
                latch.countDown();
            }

        };
        // 注册订阅者
        messageBus.register(subscriber1);

        // 等待消息处理完成（最多等待 5 秒）
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // 验证结果
        assertTrue(completed, "消息处理超时");
        assertEquals(1, subscriberCount.get(), "订阅者应该收到 1 条消息");
        log.info("[测试] 测试完成 - 订阅者收到{}条消息", subscriberCount.get());
        assertEquals(testMessage, subscriberMessageRef.get(), "接收到的消息内容不匹配");
    }

    /**
     * 1 topic 2 subscriber（pojo）
     */
    @Test
    public void testSendMessageWithTwoSubscribers() throws InterruptedException {
        // 准备
        String topic = "test-topic-" + IdUtil.simpleUUID();
//        String topic = "test-topic-pojo";
        TestMessage testMessage = new TestMessage().setNickname("yunai").setAge(18);
        // 用于等待消息处理完成
        CountDownLatch latch = new CountDownLatch(2);
        // 用于记录接收到的消息
        AtomicInteger subscriber1Count = new AtomicInteger(0);
        AtomicReference<TestMessage> subscriber1MessageRef = new AtomicReference<>();
        AtomicInteger subscriber2Count = new AtomicInteger(0);
        AtomicReference<TestMessage> subscriber2MessageRef = new AtomicReference<>();

        // 发送消息（需要提前发，保证 RocketMQ 路由的创建）
        log.info("[测试] 发送消息 - Topic: {}, Message: {}", topic, testMessage);
        messageBus.post(topic, testMessage);

        // 创建第一个订阅者
        IotMessageSubscriber<TestMessage> subscriber1 = new IotMessageSubscriber<>() {

            @Override
            public String getTopic() {
                return topic;
            }

            @Override
            public String getGroup() {
                return "test-topic-" + IdUtil.simpleUUID() + "-consumer";
//                return "test-topic-consumer-01";
            }

            @Override
            public void onMessage(TestMessage message) {
                log.info("[订阅者1] 收到消息 - Topic: {}, Message: {}", getTopic(), message);
                subscriber1Count.incrementAndGet();
                subscriber1MessageRef.set(message);
                assertEquals(testMessage, message);
                latch.countDown();
            }

        };
        // 创建第二个订阅者
        IotMessageSubscriber<TestMessage> subscriber2 = new IotMessageSubscriber<>() {

            @Override
            public String getTopic() {
                return topic;
            }

            @Override
            public String getGroup() {
                return "test-topic-" + IdUtil.simpleUUID() + "-consumer";
//                return "test-topic-consumer-02";
            }

            @Override
            public void onMessage(TestMessage message) {
                log.info("[订阅者2] 收到消息 - Topic: {}, Message: {}", getTopic(), message);
                subscriber2Count.incrementAndGet();
                subscriber2MessageRef.set(message);
                assertEquals(testMessage, message);
                latch.countDown();
            }

        };
        // 注册订阅者
        messageBus.register(subscriber1);
        messageBus.register(subscriber2);

        // 等待消息处理完成（最多等待 5 秒）
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // 验证结果
        assertTrue(completed, "消息处理超时");
        assertEquals(1, subscriber1Count.get(), "订阅者 1 应该收到 1 条消息");
        assertEquals(1, subscriber2Count.get(), "订阅者 2 应该收到 1 条消息");
        log.info("[测试] 测试完成 - 订阅者 1 收到{}条消息，订阅者2收到{}条消息", subscriber1Count.get(), subscriber2Count.get());
        assertEquals(testMessage, subscriber1MessageRef.get(), "接收到的消息内容不匹配");
        assertEquals(testMessage, subscriber2MessageRef.get(), "接收到的消息内容不匹配");
    }

    /**
     * 2 topic 2 subscriber
     */
    @Test
    public void testMultipleTopics() throws InterruptedException {
        // 准备
        String topic1 = "device-status-" + IdUtil.simpleUUID();
        String topic2 = "device-data-" + IdUtil.simpleUUID();
        String message1 = "设备在线";
        String message2 = "温度:25°C";
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger subscriber1Count = new AtomicInteger(0);
        AtomicReference<String> subscriber1MessageRef = new AtomicReference<>();
        AtomicInteger subscriber2Count = new AtomicInteger(0);
        AtomicReference<String> subscriber2MessageRef = new AtomicReference<>();


        // 发送消息到不同主题（需要提前发，保证 RocketMQ 路由的创建）
        log.info("[测试] 发送消息 - Topic1: {}, Message1: {}", topic1, message1);
        messageBus.post(topic1, message1);
        log.info("[测试] 发送消息 - Topic2: {}, Message2: {}", topic2, message2);
        messageBus.post(topic2, message2);

        // 创建订阅者 1 - 只订阅设备状态
        IotMessageSubscriber<String> statusSubscriber = new IotMessageSubscriber<>() {

            @Override
            public String getTopic() {
                return topic1;
            }

            @Override
            public String getGroup() {
                return "status-group-" + IdUtil.simpleUUID();
            }

            @Override
            public void onMessage(String message) {
                log.info("[状态订阅者] 收到消息 - Topic: {}, Message: {}", getTopic(), message);
                subscriber1Count.incrementAndGet();
                subscriber1MessageRef.set(message);
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
                return "data-group-" + IdUtil.simpleUUID();
            }

            @Override
            public void onMessage(String message) {
                log.info("[数据订阅者] 收到消息 - Topic: {}, Message: {}", getTopic(), message);
                subscriber2Count.incrementAndGet();
                subscriber2MessageRef.set(message);
                assertEquals(message2, message);
                latch.countDown();
            }

        };
        // 注册订阅者到不同主题
        messageBus.register(statusSubscriber);
        messageBus.register(dataSubscriber);

        // 等待消息处理完成
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // 验证结果
        assertTrue(completed, "消息处理超时");
        assertEquals(1, subscriber1Count.get(), "状态订阅者应该收到 1 条消息");
        assertEquals(message1, subscriber1MessageRef.get(), "状态订阅者接收到的消息内容不匹配");
        assertEquals(1, subscriber2Count.get(), "数据订阅者应该收到 1 条消息");
        assertEquals(message2, subscriber2MessageRef.get(), "数据订阅者接收到的消息内容不匹配");
        log.info("[测试] 多主题测试完成 - 状态订阅者收到{}条消息，数据订阅者收到{}条消息", subscriber1Count.get(), subscriber2Count.get());
    }

}