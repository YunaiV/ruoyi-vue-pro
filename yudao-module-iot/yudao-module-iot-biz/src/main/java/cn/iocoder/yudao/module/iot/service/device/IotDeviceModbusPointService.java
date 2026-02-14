package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusPointDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    Long createDeviceModbusPoint(@Valid IotDeviceModbusPointSaveReqVO createReqVO);

    /**
     * 更新设备 Modbus 点位配置
     *
     * @param updateReqVO 更新信息
     */
    void updateDeviceModbusPoint(@Valid IotDeviceModbusPointSaveReqVO updateReqVO);

    /**
     * 删除设备 Modbus 点位配置
     *
     * @param id 编号
     */
    void deleteDeviceModbusPoint(Long id);

    /**
     * 获得设备 Modbus 点位配置
     *
     * @param id 编号
     * @return 设备 Modbus 点位配置
     */
    IotDeviceModbusPointDO getDeviceModbusPoint(Long id);

    /**
     * 获得设备 Modbus 点位配置分页
     *
     * @param pageReqVO 分页查询
     * @return 设备 Modbus 点位配置分页
     */
    PageResult<IotDeviceModbusPointDO> getDeviceModbusPointPage(IotDeviceModbusPointPageReqVO pageReqVO);

    /**
     * 物模型变更时，更新关联点位的冗余字段（identifier、name）
     *
     * @param thingModelId 物模型编号
     * @param identifier   物模型标识符
     * @param name         物模型名称
     */
    void updateDeviceModbusPointByThingModel(Long thingModelId, String identifier, String name);

    /**
     * 根据设备编号批量获得启用的点位配置 Map
     *
     * @param deviceIds 设备编号集合
     * @return 设备点位 Map，key 为设备编号，value 为点位配置列表
     */
    Map<Long, List<IotDeviceModbusPointDO>> getEnabledDeviceModbusPointMapByDeviceIds(Collection<Long> deviceIds);

}
