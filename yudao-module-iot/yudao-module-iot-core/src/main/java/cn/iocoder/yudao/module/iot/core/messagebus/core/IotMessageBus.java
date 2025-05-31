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
     * 注册消息订阅者
     *
     * @param subscriber 订阅者
     */
    void register(IotMessageSubscriber<?> subscriber);

}