package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaPointPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaPointSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import javax.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * IoT 设备 OPC UA 点位配置 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceOpcUaPointService {

    /**
     * 创建设备 OPC UA 点位配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDeviceOpcUaPoint(@Valid IotDeviceOpcUaPointSaveReqVO createReqVO);

    /**
     * 更新设备 OPC UA 点位配置
     *
     * @param updateReqVO 更新信息
     */
    void updateDeviceOpcUaPoint(@Valid IotDeviceOpcUaPointSaveReqVO updateReqVO);

    /**
     * 删除设备 OPC UA 点位配置（逻辑删除）
     *
     * @param id 编号
     */
    void deleteDeviceOpcUaPoint(Long id);

    /**
     * 获得设备 OPC UA 点位配置
     *
     * @param id 编号
     * @return 设备 OPC UA 点位配置
     */
    IotDeviceOpcUaPointDO getDeviceOpcUaPoint(Long id);

    /**
     * 获得设备 OPC UA 点位配置分页
     *
     * @param pageReqVO 分页查询
     * @return 设备 OPC UA 点位配置分页
     */
    PageResult<IotDeviceOpcUaPointDO> getDeviceOpcUaPointPage(IotDeviceOpcUaPointPageReqVO pageReqVO);

    /**
     * 根据设备编号批量获得启用的点位配置 Map
     *
     * @param deviceIds 设备编号集合
     * @return 设备点位 Map，key 为设备编号，value 为点位配置列表
     */
    Map<Long, List<IotDeviceOpcUaPointDO>> getEnabledDeviceOpcUaPointMapByDeviceIds(Collection<Long> deviceIds);

    /**
     * 获得产品的物模型属性 Map（点位不冗余标识符，查询时据此关联；不在 Map 中的点位视为物模型失效）
     *
     * @param productIds 产品编号集合
     * @return 物模型属性 Map，key 为物模型编号
     */
    Map<Long, IotThingModelDO> getPropertyThingModelMap(Collection<Long> productIds);

}
