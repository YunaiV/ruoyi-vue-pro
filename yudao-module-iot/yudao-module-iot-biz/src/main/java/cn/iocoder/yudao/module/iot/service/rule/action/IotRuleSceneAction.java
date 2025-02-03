package cn.iocoder.yudao.module.iot.service.rule.action;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneActionTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;

import javax.annotation.Nullable;

/**
 * IOT 规则场景的场景执行器接口
 *
 * @author 芋道源码
 */
public interface IotRuleSceneAction {

    /**
     * 执行场景
     *
     * @param message 消息，允许空
     *                1. 空的情况：定时触发
     *                2. 非空的情况：设备触发
     * @param config 配置
     */
    void execute(@Nullable IotDeviceMessage message, IotRuleSceneDO.ActionConfig config);

    /**
     * 获得类型
     *
     * @return 类型
     */
    IotRuleSceneActionTypeEnum getType();

}
