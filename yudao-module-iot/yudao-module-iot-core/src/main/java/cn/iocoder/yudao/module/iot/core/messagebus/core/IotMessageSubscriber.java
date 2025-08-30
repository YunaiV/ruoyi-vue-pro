package cn.iocoder.yudao.module.iot.core.messagebus.core;

/**
 * IoT 消息总线订阅者接口
 *
 * 用于处理从消息总线接收到的消息
 *
 * @author 芋道源码
 */
public interface IotMessageSubscriber<T> {

    /**
     * @return 主题
     */
    String getTopic();

    /**
     * @return 分组
     */
    String getGroup();

    /**
     * 处理接收到的消息
     *
     * @param message 消息内容
     */
    void onMessage(T message);

}