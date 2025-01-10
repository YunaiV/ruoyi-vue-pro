package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataSimulatorSaveReqVO;

/**
 * IoT 设备日志数据 Service 接口
 *
 * @author alwayssuper
 */
public interface IotDeviceLogDataService {

    /**
     * 初始化 TDengine 表
     */
    void initTDengineSTable();

    /**
     * 模拟设备创建设备日志
     * @param simulatorReqVO 模拟设备信息
     */
    void createDeviceLog(IotDeviceDataSimulatorSaveReqVO simulatorReqVO);
}
