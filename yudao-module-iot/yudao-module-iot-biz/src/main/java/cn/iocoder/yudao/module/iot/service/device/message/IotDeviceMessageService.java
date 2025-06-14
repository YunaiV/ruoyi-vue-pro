package cn.iocoder.yudao.module.iot.service.device.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;

/**
 * IoT 设备消息 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceMessageService {

    /**
     * 初始化设备消息的 TDengine 超级表
     *
     * 系统启动时，会自动初始化一次
     */
    void defineDeviceMessageStable();

    /**
     * 发送设备消息
     *
     * @param message 消息（“codec（编解码）字段” 部分字段）
     * @param device 设备
     * @return 设备消息
     */
    IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device);

    /**
     * 发送设备消息
     *
     * @param message 消息（“codec（编解码）字段” 部分字段）
     * @return 设备消息
     */
    IotDeviceMessage sendDeviceMessage(IotDeviceMessage message);

    /**
     * 处理设备上行的消息，包括如下步骤：
     *
     * 1. 处理消息
     * 2. 记录消息
     * 3. 回复消息
     *
     * @param message 消息
     * @param device 设备
     */
    void handleUpstreamDeviceMessage(IotDeviceMessage message, IotDeviceDO device);

    /**
     * 获得设备消息分页
     *
     * @param pageReqVO 分页查询
     * @return 设备消息分页
     */
    PageResult<IotDeviceMessageDO> getDeviceMessagePage(IotDeviceMessagePageReqVO pageReqVO);

}
