package org.springframework.data.redis.stream;

import cn.hutool.core.util.ReflectUtil;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ByteRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 拓展 DefaultStreamMessageListenerContainer 实现，解决 Spring Data Redis + Redisson 结合使用时，Redisson 在 Stream 获得不到数据时，返回 null 而不是空 List，导致 NPE 异常。
 * 对应 issue：https://github.com/spring-projects/spring-data-redis/issues/2147 和 https://github.com/redisson/redisson/issues/4006
 * 目前看下来 Spring Data Redis 不肯加 null 判断，Redisson 暂时也没改返回 null 到空 List 的打算，所以暂时只能自己改，哽咽！
 *
 * @author 芋道源码
 */
public class DefaultStreamMessageListenerContainerX<K, V extends Record<K, ?>> extends DefaultStreamMessageListenerContainer<K, V> {

    /**
     * 参考 {@link StreamMessageListenerContainer#create(RedisConnectionFactory, StreamMessageListenerContainerOptions)} 的实现
     */
    public static <K, V extends Record<K, ?>> StreamMessageListenerContainer<K, V> create(RedisConnectionFactory connectionFactory, StreamMessageListenerContainer.StreamMessageListenerContainerOptions<K, V> options) {
        Assert.notNull(connectionFactory, "RedisConnectionFactory must not be null!");
        Assert.notNull(options, "StreamMessageListenerContainerOptions must not be null!");
        return new DefaultStreamMessageListenerContainerX<>(connectionFactory, options);
    }

    public DefaultStreamMessageListenerContainerX(RedisConnectionFactory connectionFactory, StreamMessageListenerContainerOptions<K, V> containerOptions) {
        super(connectionFactory, containerOptions);
    }

    /**
     * 参考 {@link DefaultStreamMessageListenerContainer#register(StreamReadRequest, StreamListener)} 的实现
     */
    @Override
    public Subscription register(StreamReadRequest<K> streamRequest, StreamListener<K, V> listener) {
        return this.doRegisterX(getReadTaskX(streamRequest, listener));
    }

    @SuppressWarnings("unchecked")
    private StreamPollTask<K, V> getReadTaskX(StreamReadRequest<K> streamRequest, StreamListener<K, V> listener) {
        StreamPollTask<K, V> task = ReflectUtil.invoke(this, "getReadTask", streamRequest, listener);
        // 修改 readFunction 方法
        Function<ReadOffset, List<ByteRecord>> readFunction = (Function<ReadOffset, List<ByteRecord>>) ReflectUtil.getFieldValue(task, "readFunction");
        ReflectUtil.setFieldValue(task, "readFunction", (Function<ReadOffset, List<ByteRecord>>) readOffset -> {
            List<ByteRecord> records = readFunction.apply(readOffset);
            //【重点】保证 records 不是空，避免 NPE 的问题！！！
            return records != null ? records : Collections.emptyList();
        });
        return task;
    }

    private Subscription doRegisterX(Task task) {
        return ReflectUtil.invoke(this, "doRegister", task);
    }

}

