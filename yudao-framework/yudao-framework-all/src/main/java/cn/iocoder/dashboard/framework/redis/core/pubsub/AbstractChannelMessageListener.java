package cn.iocoder.dashboard.framework.redis.core.pubsub;

import cn.hutool.core.util.TypeUtil;
import cn.iocoder.yudao.util.json.JsonUtils;
import lombok.SneakyThrows;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.lang.reflect.Type;

/**
 * Redis Pub/Sub 监听器抽象类，用于实现广播消费
 *
 * @param <T> 消息类型。一定要填写噢，不然会报错
 *
 * @author 芋道源码
 */
public abstract class AbstractChannelMessageListener<T extends ChannelMessage> implements MessageListener {

    /**
     * 消息类型
     */
    private final Class<T> messageType;
    /**
     * Redis Channel
     */
    private final String channel;

    @SneakyThrows
    protected AbstractChannelMessageListener() {
        this.messageType = getMessageClass();
        this.channel = messageType.newInstance().getChannel();
    }

    /**
     * 获得 Sub 订阅的 Redis Channel 通道
     *
     * @return channel
     */
    public final String getChannel() {
        return channel;
    }

    @Override
    public final void onMessage(Message message, byte[] bytes) {
        T messageObj = JsonUtils.parseObject(message.getBody(), messageType);
        this.onMessage(messageObj);
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

}
