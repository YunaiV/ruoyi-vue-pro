package cn.iocoder.yudao.module.iot.service.rule.action;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.control.IotDeviceDownstreamReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneActionTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.control.IotDeviceDownstreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * IoT 设备控制的 {@link IotRuleSceneAction} 实现类
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotRuleSceneDeviceControlAction implements IotRuleSceneAction {

    @Resource
    private IotDeviceDownstreamService deviceDownstreamService;
    @Resource
    private IotDeviceService deviceService;

    @Override
    public void execute(IotDeviceMessage message, IotRuleSceneDO.ActionConfig config) {
        IotRuleSceneDO.ActionDeviceControl control = config.getDeviceControl();
        Assert.notNull(control, "设备控制配置不能为空");
        // 遍历每个设备，下发消息
        control.getDeviceNames().forEach(deviceName -> {
            IotDeviceDO device = deviceService.getDeviceByProductKeyAndDeviceNameFromCache(control.getProductKey(), deviceName);
            if (device == null) {
                log.error("[execute][message({}) config({}) 对应的设备不存在]", message, config);
                return;
            }
            try {
                IotDeviceMessage downstreamMessage = deviceDownstreamService.downstreamDevice(new IotDeviceDownstreamReqVO()
                        .setId(device.getId()).setType(control.getType()).setIdentifier(control.getIdentifier())
                        .setData(control.getData()));
                log.info("[execute][message({}) config({}) 下发消息({})成功]", message, config, downstreamMessage);
            } catch (Exception e) {
                log.error("[execute][message({}) config({}) 下发消息失败]", message, config, e);
            }
        });
    }

    @Override
    public IotRuleSceneActionTypeEnum getType() {
        return IotRuleSceneActionTypeEnum.DEVICE_CONTROL;
    }

}
