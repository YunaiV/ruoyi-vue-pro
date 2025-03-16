package cn.iocoder.yudao.module.iot.service.rule.action;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotRuleSceneActionTypeEnum;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;

import javax.annotation.Nullable;

/**
 * IoT 规则场景的场景执行器接口
 *
 * @author 芋道源码
 */
public interface IotRuleSceneAction {

    // TODO @芋艿：groovy 或者 javascript 实现数据的转换；可以考虑基于 hutool 的 ScriptUtil 做
    /**
     * 执行场景
     *
     * @param message 消息，允许空
     *                1. 空的情况：定时触发
     *                2. 非空的情况：设备触发
     * @param config 配置
     */
    void execute(@Nullable IotDeviceMessage message, IotRuleSceneDO.ActionConfig config) throws Exception;

    /**
     * 获得类型
     *
     * @return 类型
     */
    IotRuleSceneActionTypeEnum getType();

}
