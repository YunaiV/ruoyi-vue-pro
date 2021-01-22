package cn.iocoder.dashboard.framework.redis.core.listener;

import org.springframework.data.redis.connection.MessageListener;

/**
 * Redis Pub/Sub 监听器抽象类，用于实现广播消费
 *
 * @author 芋道源码
 */
public abstract class AbstractMessageListener<T> implements MessageListener {

    /**
     * 获得 Sub 订阅的 Redis Channel 通道
     *
     * @return channel
     */
    public abstract String getChannel();

}
