package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;


/**
 * IoT 数据桥梁的执行器 execute 接口
 *
 * @author HUIHUI
 */
public interface IotDataBridgeExecute {

    // TODO @huihui：要不搞个 getType？然后 execute0 由子类实现。这样，子类的 executeRedisStream ，其实就是 execute0 了。

    /**
     * 执行数据桥梁操作
     *
     * @param message    设备消息
     * @param dataBridge 数据桥梁
     */
    void execute(IotDeviceMessage message, IotDataBridgeDO dataBridge);

}
