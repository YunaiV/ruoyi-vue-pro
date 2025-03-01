package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;


/**
 * IoT 数据桥梁的执行器 execute 接口
 *
 * @author HUIHUI
 */
public interface IotDataBridgeExecute {

    /**
     * 执行数据桥梁操作
     *
     * @param message    设备消息
     * @param dataBridge 数据桥梁
     */
    void execute(IotDeviceMessage message, IotDataBridgeDO dataBridge);

}
