package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 设备 Modbus 连接配置 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceModbusConfigService {

    /**
     * 创建设备 Modbus 连接配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createModbusConfig(@Valid IotDeviceModbusConfigSaveReqVO createReqVO);

    /**
     * 更新设备 Modbus 连接配置
     *
     * @param updateReqVO 更新信息
     */
    void updateModbusConfig(@Valid IotDeviceModbusConfigSaveReqVO updateReqVO);

    /**
     * 删除设备 Modbus 连接配置
     *
     * @param id 编号
     */
    void deleteModbusConfig(Long id);

    /**
     * 获得设备 Modbus 连接配置
     *
     * @param id 编号
     * @return 设备 Modbus 连接配置
     */
    IotDeviceModbusConfigDO getModbusConfig(Long id);

    /**
     * 根据设备编号获得 Modbus 连接配置
     *
     * @param deviceId 设备编号
     * @return 设备 Modbus 连接配置
     */
    IotDeviceModbusConfigDO getModbusConfigByDeviceId(Long deviceId);

    /**
     * 获得设备 Modbus 连接配置分页
     *
     * @param pageReqVO 分页查询
     * @return 设备 Modbus 连接配置分页
     */
    PageResult<IotDeviceModbusConfigDO> getModbusConfigPage(IotDeviceModbusConfigPageReqVO pageReqVO);

    /**
     * 获得所有启用的 Modbus 连接配置列表
     *
     * @return 启用的 Modbus 连接配置列表
     */
    List<IotDeviceModbusConfigDO> getEnabledModbusConfigList();

}
