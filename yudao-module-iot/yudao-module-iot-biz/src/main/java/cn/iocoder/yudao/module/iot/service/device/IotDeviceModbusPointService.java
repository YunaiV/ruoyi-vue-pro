package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusPointDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 设备 Modbus 点位配置 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceModbusPointService {

    /**
     * 创建设备 Modbus 点位配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createModbusPoint(@Valid IotDeviceModbusPointSaveReqVO createReqVO);

    /**
     * 更新设备 Modbus 点位配置
     *
     * @param updateReqVO 更新信息
     */
    void updateModbusPoint(@Valid IotDeviceModbusPointSaveReqVO updateReqVO);

    /**
     * 删除设备 Modbus 点位配置
     *
     * @param id 编号
     */
    void deleteModbusPoint(Long id);

    /**
     * 获得设备 Modbus 点位配置
     *
     * @param id 编号
     * @return 设备 Modbus 点位配置
     */
    IotDeviceModbusPointDO getModbusPoint(Long id);

    /**
     * 获得设备 Modbus 点位配置分页
     *
     * @param pageReqVO 分页查询
     * @return 设备 Modbus 点位配置分页
     */
    PageResult<IotDeviceModbusPointDO> getModbusPointPage(IotDeviceModbusPointPageReqVO pageReqVO);

    /**
     * 根据设备编号获得点位配置列表
     *
     * @param deviceId 设备编号
     * @return 点位配置列表
     */
    List<IotDeviceModbusPointDO> getModbusPointListByDeviceId(Long deviceId);

    /**
     * 根据设备编号获得启用的点位配置列表
     *
     * @param deviceId 设备编号
     * @return 启用的点位配置列表
     */
    List<IotDeviceModbusPointDO> getEnabledModbusPointListByDeviceId(Long deviceId);

}
