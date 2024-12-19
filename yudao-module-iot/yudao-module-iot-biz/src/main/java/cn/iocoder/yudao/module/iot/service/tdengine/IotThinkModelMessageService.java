package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThinkModelMessage;

/**
 * 物模型消息 Service
 */
public interface IotThinkModelMessageService {

    /**
     * 保存物模型消息
     *
     * @param device            设备
     * @param thingModelMessage 物模型消息
     */
    void saveThinkModelMessage(IotDeviceDO device, ThinkModelMessage thingModelMessage);

    /**
     * 创建物模型消息日志超级表
     *
     * @param productId 产品编号
     */
    void createSuperTable(Long productId);

}