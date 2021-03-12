package cn.iocoder.dashboard.modules.system.redis.stream;

import cn.iocoder.dashboard.framework.redis.core.util.RedisStreamUtils;
import cn.iocoder.dashboard.modules.system.redis.stream.sms.SmsSendMessage;
import cn.iocoder.dashboard.modules.system.redis.stream.sms.SmsSendStreamConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;


@Slf4j
@Component
public class StreamConsumerRunner implements ApplicationRunner, DisposableBean {

    @Resource
    RedisConnectionFactory redisConnectionFactory;

    @Resource
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    SmsSendStreamConsumer streamMessageListener;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    private StreamMessageListenerContainer<String, ObjectRecord<String, SmsSendMessage>> streamMessageListenerContainer;

    @Override
    public void run(ApplicationArguments args) throws UnknownHostException {

        StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups(RedisStreamUtils.KEY_SMS_SEND);
        if (groups.isEmpty()) {
            stringRedisTemplate.opsForStream().createGroup(RedisStreamUtils.KEY_SMS_SEND, RedisStreamUtils.GROUP_SMS_SEND);
        }


        // 创建配置对象
        StreamMessageListenerContainerOptions<String, ObjectRecord<String, SmsSendMessage>> streamMessageListenerContainerOptions = StreamMessageListenerContainerOptions
                .builder()
                // 一次性最多拉取多少条消息
                .batchSize(10)
                // 执行消息轮询的执行器
                .executor(this.threadPoolTaskExecutor)
                // 消息消费异常的handler
                .errorHandler(new ErrorHandler() {
                    @Override
                    public void handleError(Throwable t) {
                        // throw new RuntimeException(t);
                        t.printStackTrace();
                    }
                })
                // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                .pollTimeout(Duration.ZERO)
                // 序列化器
                .serializer(new StringRedisSerializer())
                .targetType(SmsSendMessage.class)
                .build();

        // 根据配置对象创建监听容器对象
        StreamMessageListenerContainer<String, ObjectRecord<String, SmsSendMessage>> streamMessageListenerContainer = StreamMessageListenerContainer
                .create(this.redisConnectionFactory, streamMessageListenerContainerOptions);

        // 使用监听容器对象开始监听消费（使用的是手动确认方式）
        streamMessageListenerContainer.receive(Consumer.from(RedisStreamUtils.GROUP_SMS_SEND, InetAddress.getLocalHost().getHostName()),
                StreamOffset.create(RedisStreamUtils.KEY_SMS_SEND, ReadOffset.lastConsumed()), this.streamMessageListener);

        this.streamMessageListenerContainer = streamMessageListenerContainer;
        // 启动监听
        this.streamMessageListenerContainer.start();

    }

    @Override
    public void destroy() throws Exception {
        this.streamMessageListenerContainer.stop();
    }
}