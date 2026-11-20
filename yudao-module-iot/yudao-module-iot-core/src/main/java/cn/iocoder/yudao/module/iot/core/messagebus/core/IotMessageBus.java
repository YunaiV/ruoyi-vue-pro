package cn.iocoder.yudao.module.iot.core.messagebus.core;

/**
 * IoT 消息总线接口
 *
 * 用于在 IoT 系统中发布和订阅消息，支持多种消息中间件实现
 *
 * @author 芋道源码
 */
public interface IotMessageBus {

    /**
     * 发布消息到消息总线
     *
     * @param topic 主题
     * @param message 消息内容
     */
    void post(String topic, Object message);

    /**
     * 发布消息到消息总线，并等待 broker 确认已接收：返回即已接收，未确认（失败、拒绝、无法路由、超时）时抛出异常
     * <p>
     * 用于发送成功后才能删除本地副本的场景，例如说网关的离线暂存。确认只到 broker，不代表消费者已经处理
     *
     * @param topic   主题
     * @param message 消息内容
     * @throws IotMessageRejectedException 这条消息被永久拒绝（例如说超过大小限制），重试也不会成功
     */
    void postConfirmed(String topic, Object message);

    /**
     * 校验 {@link #postConfirmed} 可用：配置不能确认发送时（例如说 Kafka 的 acks=0），抛出异常
     * <p>
     * 用于启动时提前发现配置问题，例如说网关开启离线暂存时
     *
     * @throws IllegalStateException 配置不能确认发送
     */
    default void validateConfirmable() {
    }

    /**
     * 注册消息订阅者
     *
     * @param subscriber 订阅者
     */
    void register(IotMessageSubscriber<?> subscriber);

    /**
     * 取消注册消息订阅者
     *
     * @param subscriber 订阅者
     */
    default void unregister(IotMessageSubscriber<?> subscriber) {
        // TODO 芋艿：暂时不实现，需求量不大，但是
        // throw new UnsupportedOperationException("取消注册消息订阅者功能，尚未实现");
    }

}