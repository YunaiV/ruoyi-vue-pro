package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.codec.tcp.IotTcpDeviceMessageCodec;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 TCP 下行消息处理器
 * <p>
 * 负责处理从业务系统发送到设备的下行消息，包括：
 * 1. 属性设置
 * 2. 服务调用
 * 3. 属性获取
 * 4. 配置下发
 * 5. OTA 升级
 * <p>
 * 注意：由于移除了连接管理器，此处理器主要负责消息的编码和日志记录
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpDownstreamHandler {

    private final IotDeviceMessageService messageService;

    private final IotTcpDeviceMessageCodec codec;

    public IotTcpDownstreamHandler(IotDeviceMessageService messageService) {
        this.messageService = messageService;
        this.codec = new IotTcpDeviceMessageCodec();
    }

    /**
     * 处理下行消息
     *
     * @param message 设备消息
     */
    public void handle(IotDeviceMessage message) {
        try {
            log.info("[handle][处理下行消息] 设备ID: {}, 方法: {}, 消息ID: {}",
                    message.getDeviceId(), message.getMethod(), message.getId());

            // 编码消息用于日志记录和验证
            byte[] encodedMessage = codec.encode(message);
            log.debug("[handle][消息编码成功] 设备ID: {}, 编码后长度: {} 字节",
                    message.getDeviceId(), encodedMessage.length);

            // 记录下行消息处理
            log.info("[handle][下行消息处理完成] 设备ID: {}, 方法: {}, 消息内容: {}",
                    message.getDeviceId(), message.getMethod(), message.getParams());

        } catch (Exception e) {
            log.error("[handle][处理下行消息失败] 设备ID: {}, 方法: {}, 消息内容: {}",
                    message.getDeviceId(), message.getMethod(), message.getParams(), e);
        }
    }

}