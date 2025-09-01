package cn.iocoder.yudao.module.iot.service.rule.scene.action;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleActionTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * IoT 设备控制的 {@link IotSceneRuleAction} 实现类
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotDeviceControlSceneRuleAction implements IotSceneRuleAction {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDeviceMessageService deviceMessageService;

    // TODO @puhui999：这里
    @Override
    public void execute(IotDeviceMessage message,
                        IotSceneRuleDO rule, IotSceneRuleDO.Action actionConfig) {
        //IotSceneRuleDO.ActionDeviceControl control = actionConfig.getDeviceControl();
        //Assert.notNull(control, "设备控制配置不能为空");
        //// 遍历每个设备，下发消息
        //control.getDeviceNames().forEach(deviceName -> {
        //    IotDeviceDO device = deviceService.getDeviceFromCache(control.getProductKey(), deviceName);
        //    if (device == null) {
        //        log.error("[execute][message({}) actionConfig({}) 对应的设备不存在]", message, actionConfig);
        //        return;
        //    }
        //    try {
        //        // TODO @芋艿：@puhui999：这块可能要改，从 type => method
        //        IotDeviceMessage downstreamMessage = deviceMessageService.sendDeviceMessage(IotDeviceMessage.requestOf(
        //                control.getType() + control.getIdentifier(), control.getData()).setDeviceId(device.getId()));
        //        log.info("[execute][message({}) actionConfig({}) 下发消息({})成功]", message, actionConfig, downstreamMessage);
        //    } catch (Exception e) {
        //        log.error("[execute][message({}) actionConfig({}) 下发消息失败]", message, actionConfig, e);
        //    }
        //});
    }

    @Override
    public IotSceneRuleActionTypeEnum getType() {
        return IotSceneRuleActionTypeEnum.DEVICE_PROPERTY_SET;
    }

}
