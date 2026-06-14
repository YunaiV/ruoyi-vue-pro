package cn.iocoder.yudao.module.iot.service.device.shadow;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceShadowDO;

import java.util.Map;

/**
 * IoT 设备影子 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceShadowService {

    /**
     * 获取或创建设备影子
     *
     * @param device 设备
     * @return 设备影子
     */
    IotDeviceShadowDO getOrCreateShadow(IotDeviceDO device);

    /**
     * 处理设备影子上行更新
     *
     * @param device  设备
     * @param message 上行消息
     */
    void handleDeviceShadowUpdate(IotDeviceDO device, IotDeviceMessage message);

    /**
     * 更新期望状态
     *
     * @param deviceId 设备编号
     * @param desired  期望状态
     */
    void updateDesiredState(Long deviceId, Map<String, Object> desired);

    /**
     * 重置影子
     *
     * @param deviceId 设备编号
     */
    void resetShadow(Long deviceId);

    /**
     * 更新影子开关状态
     *
     * @param deviceId 设备编号
     * @param status   状态
     */
    void updateStatus(Long deviceId, Integer status);

    /**
     * 删除设备影子（设备删除时级联）
     *
     * @param deviceId 设备编号
     */
    void deleteByDeviceId(Long deviceId);

}
