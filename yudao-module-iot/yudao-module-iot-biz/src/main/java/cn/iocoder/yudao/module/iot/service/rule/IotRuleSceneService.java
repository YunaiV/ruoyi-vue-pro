package cn.iocoder.yudao.module.iot.service.rule;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;

import java.util.List;

/**
 * IoT 规则场景 Service 接口
 *
 * @author 芋道源码
 */
public interface IotRuleSceneService {

    /**
     * 【缓存】获得指定设备的场景列表
     *
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 场景列表
     */
    List<IotRuleSceneDO> getRuleSceneListByProductKeyAndDeviceNameFromCache(String productKey, String deviceName);

    /**
     * 执行规则场景
     *
     * @param message 消息
     */
    void executeRuleScene(IotDeviceMessage message);

}
