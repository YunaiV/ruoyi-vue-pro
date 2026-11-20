package cn.iocoder.yudao.module.iot.core.messagebus.core;

/**
 * 消息被消息总线永久拒绝：同一条消息重试也不会成功，例如说消息超过大小限制
 * <p>
 * 由 {@link IotMessageBus#postConfirmed} 抛出，只用于有明确证据的单条拒绝（Kafka 的 RecordTooLargeException、
 * RocketMQ 客户端校验、broker 返回的 MESSAGE_ILLEGAL）。broker 故障、超时、NACK、无法路由等都不使用，仍按未确认处理
 *
 * @author 芋道源码
 */
public class IotMessageRejectedException extends RuntimeException {

    public IotMessageRejectedException(String message, Throwable cause) {
        super(message, cause);
    }

}
