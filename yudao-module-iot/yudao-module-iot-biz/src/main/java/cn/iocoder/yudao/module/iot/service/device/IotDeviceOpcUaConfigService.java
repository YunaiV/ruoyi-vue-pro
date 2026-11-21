package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigListReqDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaConfigDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 设备 OPC UA 连接配置 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceOpcUaConfigService {

    /**
     * 保存设备 OPC UA 连接配置（新增或更新）：每台设备只有一份未删除的配置
     *
     * @param saveReqVO 保存信息
     */
    void saveDeviceOpcUaConfig(@Valid IotDeviceOpcUaConfigSaveReqVO saveReqVO);

    /**
     * 获得设备 OPC UA 连接配置
     *
     * @param id 编号
     * @return 设备 OPC UA 连接配置
     */
    IotDeviceOpcUaConfigDO getDeviceOpcUaConfig(Long id);

    /**
     * 根据设备编号获得 OPC UA 连接配置
     *
     * @param deviceId 设备编号
     * @return 设备 OPC UA 连接配置
     */
    IotDeviceOpcUaConfigDO getDeviceOpcUaConfigByDeviceId(Long deviceId);

    /**
     * 获得 OPC UA 连接配置列表
     *
     * @param listReqDTO 查询参数
     * @return OPC UA 连接配置列表
     */
    List<IotDeviceOpcUaConfigDO> getDeviceOpcUaConfigList(IotOpcUaDeviceConfigListReqDTO listReqDTO);

}
