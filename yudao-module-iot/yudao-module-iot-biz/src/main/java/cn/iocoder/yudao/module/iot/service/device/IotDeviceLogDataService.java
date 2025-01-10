package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataSimulatorSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;

/**
 * IoT 设备日志数据 Service 接口
 *
 * @author alwayssuper
 */
public interface IotDeviceLogDataService {

    /**
     * 初始化 TDengine 超级表
     *
     *系统启动时，会自动初始化一次
     */
    void initTDengineSTable();

    /**
     * 插入设备日志
     *
     * 当该设备第一次插入日志时，自动创建该设备的设备日志子表
     *
     * @param simulatorReqVO 设备日志模拟数据
     */
    void createDeviceLog(IotDeviceDataSimulatorSaveReqVO simulatorReqVO);

    /**
     * 获得设备日志分页
     *
     * @param pageReqVO 分页查询
     * @return 设备日志分页
     */
    PageResult<IotDeviceLogDO> getDeviceLogPage(IotDeviceLogPageReqVO pageReqVO);

}
