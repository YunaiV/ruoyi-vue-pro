package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;


/**
 * IoT 数据桥梁的执行器 execute 接口
 *
 * @author HUIHUI
 */
public interface IotDataBridgeExecute<Config> {

    /**
     * 获取数据桥梁类型
     *
     * @return 数据桥梁类型
     */
    Integer getType();

    /**
     * 执行数据桥梁操作
     *
     * @param message    设备消息
     * @param dataBridge 数据桥梁
     */
    @SuppressWarnings({"unchecked"})
    default void execute(IotDeviceMessage message, IotDataBridgeDO dataBridge) throws Exception {
        // 1.1 校验数据桥梁类型
        if (!getType().equals(dataBridge.getType())) {
            return;
        }

        // 1.2 执行对应的数据桥梁发送消息
        execute0(message, (Config) dataBridge.getConfig());
    }

    /**
     * 【真正】执行数据桥梁操作
     *
     * @param message 设备消息
     * @param config  桥梁配置
     */
    void execute0(IotDeviceMessage message, Config config) throws Exception;

}
