package cn.iocoder.dashboard.framework.redis.core.listener;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.dashboard.util.json.JSONUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;

/**
 * Redis Pub/Sub 监听器抽象类，用于实现广播消费
 *
 * @param <T> 消息类型。一定要填写噢，不然会报错
 *
 * @author 芋道源码
 */
public abstract class AbstractMessageListener<T> implements MessageListener {

    /**
     * 消息类型
     */
    private final Class<T> messageType;

    protected AbstractMessageListener() {
        this.messageType = getMessageClass();
    }

    /**
     * 获得 Sub 订阅的 Redis Channel 通道
     *
     * @return channel
     */
    public abstract String getChannel();

    @Override
    public final void onMessage(Message message, byte[] bytes) {
        T messageObj = JSONUtils.parseObject(message.getBody(), messageType);
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
        Class<?> targetClass = getClass();
        while (targetClass.getSuperclass() != null) {
            // 如果不是 AbstractMessageListener 父类，继续向上查找
            if (targetClass.getSuperclass() != AbstractMessageListener.class) {
                targetClass = targetClass.getSuperclass();
                continue;
            }
            // 如果是 AbstractMessageListener 父类，则解析泛型
            Type[] types = ((ParameterizedTypeImpl) targetClass.getGenericSuperclass()).getActualTypeArguments();
            if (ArrayUtil.isEmpty(types)) {
                throw new IllegalStateException(String.format("类型(%s) 需要设置消息类型", getClass().getName()));
            }
            return (Class<T>) types[0];
        }
        throw new IllegalStateException(String.format("类型(%s) 找不到 AbstractMessageListener 父类", getClass().getName()));
    }

}
