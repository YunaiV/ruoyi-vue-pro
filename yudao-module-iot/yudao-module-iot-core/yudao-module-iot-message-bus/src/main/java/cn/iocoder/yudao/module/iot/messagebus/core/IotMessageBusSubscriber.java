package cn.iocoder.yudao.module.iot.messagebus.core;

/**
 * IoT 消息总线订阅者接口
 *
 * 用于处理从消息总线接收到的消息
 *
 * @author 芋道源码
 */
public interface IotMessageBusSubscriber<T> {

    /**
     * 处理接收到的消息
     *
     * @param topic 主题
     * @param message 消息内容
     */
    void onMessage(String topic, T message);

    /**
     * 获取订阅者的顺序
     *
     * @return 顺序值
     */
    int order();

}