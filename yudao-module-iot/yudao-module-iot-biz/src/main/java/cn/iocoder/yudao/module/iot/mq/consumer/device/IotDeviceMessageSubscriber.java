package cn.iocoder.yudao.module.iot.mq.consumer.device;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageSubscriber;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 针对 {@link IotDeviceMessage} 的业务处理器：调用 method 对应的逻辑。例如说：
 * 1. {@link IotDeviceMessageMethodEnum#PROPERTY_POST} 属性上报时，记录设备属性
 *
 * @author alwayssuper
 */
@Component
@Slf4j
public class IotDeviceMessageSubscriber implements IotMessageSubscriber<IotDeviceMessage> {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    private IotDeviceMessageService deviceMessageService;

    @Resource
    private IotMessageBus messageBus;

    @PostConstruct
    public void init() {
        messageBus.register(this);
    }

    @Override
    public String getTopic() {
        return IotDeviceMessage.MESSAGE_BUS_DEVICE_MESSAGE_TOPIC;
    }

    @Override
    public String getGroup() {
        return "iot_device_message_consumer";
    }

    @Override
    public void onMessage(IotDeviceMessage message) {
        if (!IotDeviceMessageUtils.isUpstreamMessage(message)) {
            log.error("[onMessage][message({}) 非上行消息，不进行处理]", message);
            return;
        }

        TenantUtils.execute(message.getTenantId(), () -> {
            // 1.1 更新设备的最后时间
            IotDeviceDO device = deviceService.validateDeviceExistsFromCache(message.getDeviceId());
            devicePropertyService.updateDeviceReportTimeAsync(device.getId(), LocalDateTime.now());
            // 1.2 更新设备的连接 server
            // TODO 芋艿：HTTP 网关的上行消息，不应该更新 serverId，会覆盖掉 MQTT 等长连接的 serverId，导致下行消息无法发送。
            devicePropertyService.updateDeviceServerIdAsync(device.getId(), message.getServerId());

            // 2. 未上线的设备，强制上线
            forceDeviceOnline(message, device);

            // 3. 核心：处理消息
            deviceMessageService.handleUpstreamDeviceMessage(message, device);
        });
    }

    private void forceDeviceOnline(IotDeviceMessage message, IotDeviceDO device) {
        // 已经在线，无需处理
        if (ObjectUtil.equal(device.getState(), IotDeviceStateEnum.ONLINE.getState())) {
            return;
        }
        // 如果是 STATE 相关的消息，无需处理，不然就重复处理状态了
        if (Objects.equals(message.getMethod(), IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod())) {
            return;
        }

        // 特殊：设备非在线时，主动标记设备为在线
        // 为什么不直接更新状态呢？因为通过 IotDeviceMessage 可以经过一系列的处理，例如说记录日志、规则引擎等等
        try {
            deviceMessageService.sendDeviceMessage(IotDeviceMessage.buildStateUpdateOnline().setDeviceId(device.getId()));
        } catch (Exception e) {
            // 注意：即使执行失败，也不影响主流程
            log.error("[forceDeviceOnline][message({}) device({}) 强制设备上线失败]", message, device, e);
        }
    }

}
