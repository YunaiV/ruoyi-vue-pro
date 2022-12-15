package cn.iocoder.yudao.framework.mq.scheduler;

import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.mq.core.stream.AbstractStreamMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;

/**
 * 这个定时器用于处理，crash 之后的消费者未消费完的消息
 */
@Slf4j
@EnableScheduling
public class PendingMessageScheduler {

    @Autowired
    private List<AbstractStreamMessageListener<?>> listeners;
    @Autowired
    private RedisMQTemplate redisTemplate;
    @Value("${spring.application.name}")
    private String groupName;

    /**
     * 一分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void processPendingMessage() {
        StreamOperations<String, Object, Object> ops = redisTemplate.getRedisTemplate().opsForStream();

        for (AbstractStreamMessageListener<?> listener : listeners) {
            PendingMessagesSummary pendingMessagesSummary = ops.pending(listener.getStreamKey(), groupName);
            // 每个消费者的pending消息数量
            Map<String, Long> pendingMessagesPerConsumer = pendingMessagesSummary.getPendingMessagesPerConsumer();
            pendingMessagesPerConsumer.entrySet().forEach(entry -> {
                String consumerName = entry.getKey();
                Long pendingMessageCount = entry.getValue();
                log.info("[processPendingMessage][消费者({}) 消息数量({})]", consumerName, pendingMessageCount);

                // 从消费者的pending队列中读取消息
                List<MapRecord<String, Object, Object>> retVal = ops.read(Consumer.from(groupName, consumerName), StreamOffset.create(listener.getStreamKey(), ReadOffset.from("0")));

                for (MapRecord<String, Object, Object> record : retVal) {
                    // 重新投递消息
                    redisTemplate.getRedisTemplate().opsForStream().add(StreamRecords.newRecord()
                            .ofObject(record.getValue()) // 设置内容
                            .withStreamKey(listener.getStreamKey()));

                    // ack 消息消费完成
                    redisTemplate.getRedisTemplate().opsForStream().acknowledge(groupName, record);
                }
            });
        }
    }
}
