package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusDeviceConfigListReqDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;

import javax.validation.Valid;
import java.util.List;

/**
 * IoT 设备 Modbus 连接配置 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceModbusConfigService {

    /**
     * 保存设备 Modbus 连接配置（新增或更新）
     *
     * @param saveReqVO 保存信息
     */
    void saveDeviceModbusConfig(@Valid IotDeviceModbusConfigSaveReqVO saveReqVO);

    /**
     * 获得设备 Modbus 连接配置
     *
     * @param id 编号
     * @return 设备 Modbus 连接配置
     */
    IotDeviceModbusConfigDO getDeviceModbusConfig(Long id);

    /**
     * 根据设备编号获得 Modbus 连接配置
     *
     * @param deviceId 设备编号
     * @return 设备 Modbus 连接配置
     */
    IotDeviceModbusConfigDO getDeviceModbusConfigByDeviceId(Long deviceId);

    /**
     * 获得 Modbus 连接配置列表
     *
     * @param listReqDTO 查询参数
     * @return Modbus 连接配置列表
     */
    List<IotDeviceModbusConfigDO> getDeviceModbusConfigList(IotModbusDeviceConfigListReqDTO listReqDTO);

}
