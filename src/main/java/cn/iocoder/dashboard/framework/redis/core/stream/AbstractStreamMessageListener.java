package cn.iocoder.dashboard.framework.redis.core.stream;

import cn.hutool.core.util.ArrayUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;

/**
 * Redis Stream 监听器抽象类，用于实现集群消费
 *
 * @param <T> 消息类型。一定要填写噢，不然会报错
 *
 * @author 芋道源码
 */
public abstract class AbstractStreamMessageListener<T extends StreamMessage>
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
    @Value("spring.application.name")
    @Getter
    private String group;

    @SneakyThrows
    protected AbstractStreamMessageListener() {
        this.messageType = getMessageClass();
        this.streamKey = messageType.newInstance().getStreamKey();
    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        System.out.println(message);
        if (true) {
//            throw new IllegalStateException("测试下");
        }
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
    // TODO 芋艿：稍后重构
    private Class<T> getMessageClass() {
        Class<?> targetClass = getClass();
        while (targetClass.getSuperclass() != null) {
            // 如果不是 AbstractMessageListener 父类，继续向上查找
            if (targetClass.getSuperclass() != AbstractStreamMessageListener.class) {
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
        throw new IllegalStateException(String.format("类型(%s) 找不到 AbstractStreamMessageListener 父类", getClass().getName()));
    }

}
