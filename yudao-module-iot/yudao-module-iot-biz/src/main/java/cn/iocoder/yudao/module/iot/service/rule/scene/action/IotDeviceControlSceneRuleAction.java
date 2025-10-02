package cn.iocoder.yudao.module.iot.service.rule.scene.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleActionTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * IoT 设备属性设置的 {@link IotSceneRuleAction} 实现类
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

    @Override
    public void execute(IotDeviceMessage message,
                        IotSceneRuleDO rule, IotSceneRuleDO.Action actionConfig) {
        // 1. 参数校验
        if (actionConfig.getDeviceId() == null) {
            log.error("[execute][规则场景({}) 动作配置({}) 设备编号不能为空]", rule.getId(), actionConfig);
            return;
        }
        if (StrUtil.isEmpty(actionConfig.getIdentifier())) {
            log.error("[execute][规则场景({}) 动作配置({}) 属性标识符不能为空]", rule.getId(), actionConfig);
            return;
        }

        // 2. 判断是否为全部设备
        if (IotDeviceDO.DEVICE_ID_ALL.equals(actionConfig.getDeviceId())) {
            executeForAllDevices(message, rule, actionConfig);
        } else {
            executeForSingleDevice(message, rule, actionConfig);
        }
    }

    /**
     * 为单个设备执行属性设置
     */
    private void executeForSingleDevice(IotDeviceMessage message,
                                        IotSceneRuleDO rule, IotSceneRuleDO.Action actionConfig) {
        // 1. 获取设备信息
        IotDeviceDO device = deviceService.getDeviceFromCache(actionConfig.getDeviceId());
        if (device == null) {
            log.error("[executeForSingleDevice][规则场景({}) 动作配置({}) 对应的设备({}) 不存在]",
                    rule.getId(), actionConfig, actionConfig.getDeviceId());
            return;
        }

        // 2. 执行属性设置
        executePropertySetForDevice(rule, actionConfig, device);
    }

    /**
     * 为产品下的所有设备执行属性设置
     */
    private void executeForAllDevices(IotDeviceMessage message,
                                      IotSceneRuleDO rule, IotSceneRuleDO.Action actionConfig) {
        // 1. 参数校验
        if (actionConfig.getProductId() == null) {
            log.error("[executeForAllDevices][规则场景({}) 动作配置({}) 产品编号不能为空]", rule.getId(), actionConfig);
            return;
        }

        // 2. 获取产品下的所有设备
        List<IotDeviceDO> devices = deviceService.getDeviceListByProductId(actionConfig.getProductId());
        if (CollUtil.isEmpty(devices)) {
            log.warn("[executeForAllDevices][规则场景({}) 动作配置({}) 产品({}) 下没有设备]",
                    rule.getId(), actionConfig, actionConfig.getProductId());
            return;
        }

        // 3. 遍历所有设备执行属性设置
        for (IotDeviceDO device : devices) {
            executePropertySetForDevice(rule, actionConfig, device);
        }
    }

    /**
     * 为指定设备执行属性设置
     */
    private void executePropertySetForDevice(IotSceneRuleDO rule, IotSceneRuleDO.Action actionConfig, IotDeviceDO device) {
        // 1. 构建属性设置消息
        IotDeviceMessage downstreamMessage = buildPropertySetMessage(actionConfig, device);
        if (downstreamMessage == null) {
            log.error("[executePropertySetForDevice][规则场景({}) 动作配置({}) 设备({}) 构建属性设置消息失败]",
                    rule.getId(), actionConfig, device.getId());
            return;
        }

        // 2. 发送设备消息
        try {
            IotDeviceMessage result = deviceMessageService.sendDeviceMessage(downstreamMessage, device);
            log.info("[executePropertySetForDevice][规则场景({}) 动作配置({}) 设备({}) 属性设置消息({}) 发送成功]",
                    rule.getId(), actionConfig, device.getId(), result.getId());
        } catch (Exception e) {
            log.error("[executePropertySetForDevice][规则场景({}) 动作配置({}) 设备({}) 属性设置消息发送失败]",
                    rule.getId(), actionConfig, device.getId(), e);
        }
    }

    /**
     * 构建属性设置消息
     *
     * @param actionConfig 动作配置
     * @param device       设备信息
     * @return 设备消息
     */
    private IotDeviceMessage buildPropertySetMessage(IotSceneRuleDO.Action actionConfig, IotDeviceDO device) {
        try {
            // 属性设置参数格式: {"properties": {"identifier": value}}
            Object params = Map.of("properties", Map.of(actionConfig.getIdentifier(), actionConfig.getParams()));
            return IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.PROPERTY_SET.getMethod(), params);
        } catch (Exception e) {
            log.error("[buildPropertySetMessage][构建属性设置消息异常]", e);
            return null;
        }
    }

    @Override
    public IotSceneRuleActionTypeEnum getType() {
        return IotSceneRuleActionTypeEnum.DEVICE_PROPERTY_SET;
    }

}
