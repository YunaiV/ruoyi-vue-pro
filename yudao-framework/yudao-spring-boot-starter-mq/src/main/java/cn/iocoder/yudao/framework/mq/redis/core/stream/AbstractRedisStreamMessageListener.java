package cn.iocoder.yudao.framework.mq.redis.core.stream;

import cn.hutool.core.util.TypeUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mq.redis.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.mq.redis.core.interceptor.RedisMessageInterceptor;
import cn.iocoder.yudao.framework.mq.redis.core.message.AbstractRedisMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Redis Stream 监听器抽象类，用于实现集群消费
 *
 * @param <T> 消息类型。一定要填写噢，不然会报错
 *
 * @author 芋道源码
 */
@Slf4j
public abstract class AbstractRedisStreamMessageListener<T extends AbstractRedisStreamMessage>
        implements StreamListener<String, ObjectRecord<String, String>> {

    /**
     * 消息类型
     */
    private final Class<T> messageType;
    /**
     * Redis Channel
     */
    @Getter
    private final String streamKey;

    /**
     * Redis 消费者分组，默认使用 spring.application.name 名字
     */
    @Value("${spring.application.name}")
    @Getter
    private String group;
    /**
     * RedisMQTemplate
     */
    @Setter
    private RedisMQTemplate redisMQTemplate;

    @SneakyThrows
    protected AbstractRedisStreamMessageListener() {
        this.messageType = getMessageClass();
        this.streamKey = messageType.getDeclaredConstructor().newInstance().getStreamKey();
    }

    protected AbstractRedisStreamMessageListener(String streamKey, String group) {
        this.messageType = null;
        this.streamKey = streamKey;
        this.group = group;
    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        // 消费消息
        T messageObj = JsonUtils.parseObject(message.getValue(), messageType);
        try {
            consumeMessageBefore(messageObj);
            // 消费消息
            this.onMessage(messageObj);
            // TODO 芋艿：需要额外考虑以下几个点：
            // 1. 发送日志；以及事务的结合
            // 2. 消费日志；以及通用的幂等性
            // 3. 消费失败的重试，https://zhuanlan.zhihu.com/p/60501638
        } catch (ServiceException ex) {
            // 业务拒绝（例如数据不存在）：重试也无法成功，记录日志后 ack。
            // 若不 ack，消息会滞留 pending 被 RedisPendingMessageResendJob 周期性重新投递，
            // 重投的消息再次消费再次失败，形成无限循环；pending 膨胀后，
            // 重投任务的全量 XPENDING 扫描还会拖垮 Redis
            log.warn("[onMessage][消息({}) 业务消费失败，已记录并确认，不再重试]", message.getId(), ex);
            redisMQTemplate.getRedisTemplate().opsForStream().acknowledge(group, message);
            return;
        } catch (Throwable ex) {
            // 非业务异常（例如数据库/Redis 瞬时故障）：记录日志但不 ack，保留在 pending 中，
            // 由 RedisPendingMessageResendJob 在 5 分钟后重新投递重试；
            // 若持续故障导致 pending 超过上限，由该任务的 pending 上限保护兜底丢弃
            log.error("[onMessage][消息({}) 消费失败，已记录，等待重投任务重试]", message.getId(), ex);
            return;
        } finally {
            consumeMessageAfter(messageObj);
        }
        // ack 消息消费完成
        redisMQTemplate.getRedisTemplate().opsForStream().acknowledge(group, message);
    }

    /**
     * 处理消息
     *
     * @param message 消息
     */
    public abstract void onMessage(T message);

    /**
     * 通过解析类上的泛型，获得消息类型
     *
     * @return 消息类型
     */
    @SuppressWarnings("unchecked")
    private Class<T> getMessageClass() {
        Type type = TypeUtil.getTypeArgument(getClass(), 0);
        if (type == null) {
            throw new IllegalStateException(String.format("类型(%s) 需要设置消息类型", getClass().getName()));
        }
        return (Class<T>) type;
    }

    private void consumeMessageBefore(AbstractRedisMessage message) {
        assert redisMQTemplate != null;
        List<RedisMessageInterceptor> interceptors = redisMQTemplate.getInterceptors();
        // 正序
        interceptors.forEach(interceptor -> interceptor.consumeMessageBefore(message));
    }

    private void consumeMessageAfter(AbstractRedisMessage message) {
        assert redisMQTemplate != null;
        List<RedisMessageInterceptor> interceptors = redisMQTemplate.getInterceptors();
        // 倒序
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).consumeMessageAfter(message);
        }
    }

}
