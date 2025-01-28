package cn.iocoder.yudao.module.iot.service.device.data;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;

/**
 * IoT 设备日志数据 Service 接口
 *
 * @author alwayssuper
 */
public interface IotDeviceLogService {

    /**
     * 初始化 TDengine 超级表
     *
     * 系统启动时，会自动初始化一次
     */
    void defineDeviceLog();

    /**
     * 插入设备日志
     *
     * @param message 设备数据
     */
    void createDeviceLog(IotDeviceMessage message);

    /**
     * 获得设备日志分页
     *
     * @param pageReqVO 分页查询
     * @return 设备日志分页
     */
    PageResult<IotDeviceLogDO> getDeviceLogPage(IotDeviceLogPageReqVO pageReqVO);

}
