package cn.iocoder.yudao.module.iot.gateway.service.device.spool;

import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;

/**
 * IoT 网关离线暂存 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceMessageSpoolService {

    /**
     * 离线暂存是否已启用：已开启，且消息总线支持（非 Local）、配置校验通过
     *
     * @return 是否已启用
     */
    boolean isEnabled();

    /**
     * 判断消息是否走离线暂存
     *
     * @param message  消息
     * @param protocol 协议
     * @return 是否走离线暂存
     */
    boolean isSpoolableMessage(IotDeviceMessage message, IotProtocolTypeEnum protocol);

    /**
     * 发送消息：消息总线不可用时，写入离线暂存
     *
     * @param message   消息
     * @param spoolTime 网关接收时刻
     */
    void sendSpoolableMessage(IotDeviceMessage message, long spoolTime);

}
