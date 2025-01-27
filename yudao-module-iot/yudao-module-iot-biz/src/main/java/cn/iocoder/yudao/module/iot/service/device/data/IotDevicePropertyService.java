package cn.iocoder.yudao.module.iot.service.device.data;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataSimulatorSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * IoT 设备【属性】数据 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDevicePropertyService {

    /**
     * 定义设备属性数据的结构
     *
     * @param productId 产品编号
     */
    void defineDevicePropertyData(Long productId);

    /**
     * 保存设备数据
     *
     * @param message 设备消息
     */
    void saveDeviceProperty(IotDeviceMessage message);

    /**
     * 模拟设备
     *
     * @param simulatorReqVO 设备数据
     */
    void simulatorSend(IotDeviceDataSimulatorSaveReqVO simulatorReqVO);

    /**
     * 获得设备属性最新数据
     *
     * @param deviceId 设备编号
     * @return 设备属性最新数据
     */
    List<IotDevicePropertyDO> getLatestDeviceProperties(@Valid IotDeviceDataPageReqVO deviceId);

    /**
     * 获得设备属性历史数据
     *
     * @param deviceDataReqVO 设备属性历史数据 Request VO
     * @return 设备属性历史数据
     */
    PageResult<Map<String, Object>> getHistoryDeviceProperties(@Valid IotDeviceDataPageReqVO deviceDataReqVO);

}