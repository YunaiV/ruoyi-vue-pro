package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.IotTcpSessionManager;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 TCP 下行消息处理器
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpDownstreamHandler {

    private final IotDeviceMessageService messageService;

    private final IotDeviceService deviceService;

    private final IotTcpSessionManager sessionManager;

    // TODO @haohao：这个可以使用 lombok 简化构造方法
    public IotTcpDownstreamHandler(IotDeviceMessageService messageService,
                                   IotDeviceService deviceService, IotTcpSessionManager sessionManager) {
        this.messageService = messageService;
        this.deviceService = deviceService;
        this.sessionManager = sessionManager;
    }

    /**
     * 处理下行消息
     *
     * @param message 设备消息
     */
    public void handle(IotDeviceMessage message) {
        try {
            log.info("[handle][处理下行消息] 设备 ID: {}, 方法: {}, 消息 ID: {}",
                    message.getDeviceId(), message.getMethod(), message.getId());

            // TODO @haohao 1. 和 2. 可以合成 1.1 1.2 并且中间可以不空行；
            // 1. 获取设备信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(message.getDeviceId());
            if (device == null) {
                log.error("[handle][设备不存在] 设备 ID: {}", message.getDeviceId());
                return;
            }

            // 2. 检查设备是否在线
            if (!sessionManager.isDeviceOnline(message.getDeviceId())) {
                log.warn("[handle][设备不在线] 设备 ID: {}", message.getDeviceId());
                return;
            }

            // 3. 编码消息
            byte[] bytes = messageService.encodeDeviceMessage(message, device.getCodecType());

            // 4. 发送消息到设备
            boolean success = sessionManager.sendToDevice(message.getDeviceId(), bytes);
            if (success) {
                log.info("[handle][下行消息发送成功] 设备 ID: {}, 方法: {}, 消息 ID: {}, 数据长度: {} 字节",
                        message.getDeviceId(), message.getMethod(), message.getId(), bytes.length);
            } else {
                log.error("[handle][下行消息发送失败] 设备 ID: {}, 方法: {}, 消息 ID: {}",
                        message.getDeviceId(), message.getMethod(), message.getId());
            } // TODO @haohao：下面这个空行，可以考虑去掉的哈。

        } catch (Exception e) {
            log.error("[handle][处理下行消息失败] 设备 ID: {}, 方法: {}, 消息内容: {}",
                    message.getDeviceId(), message.getMethod(), message.getParams(), e);
        }
    }

}