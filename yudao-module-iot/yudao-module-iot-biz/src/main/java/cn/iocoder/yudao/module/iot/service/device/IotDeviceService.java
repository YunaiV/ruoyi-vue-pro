package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStateEnum;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * IoT 设备 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceService {

    /**
     * 【管理员】创建设备
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDevice(@Valid IotDeviceSaveReqVO createReqVO);

    /**
     * 【设备注册】创建设备
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @param gatewayId  网关设备 ID
     * @return 设备
     */
    IotDeviceDO createDevice(@NotEmpty(message = "产品标识不能为空") String productKey,
                             @NotEmpty(message = "设备名称不能为空") String deviceName,
                             Long gatewayId);

    /**
     * 更新设备
     *
     * @param updateReqVO 更新信息
     */
    void updateDevice(@Valid IotDeviceSaveReqVO updateReqVO);

    // TODO @芋艿：先这么实现。未来看情况，要不要自己实现

    /**
     * 更新设备的所属网关
     *
     * @param id        编号
     * @param gatewayId 网关设备 ID
     */
    default void updateDeviceGateway(Long id, Long gatewayId) {
        updateDevice(new IotDeviceSaveReqVO().setId(id).setGatewayId(gatewayId));
    }

    /**
     * 更新设备状态
     *
     * @param id    编号
     * @param state 状态
     */
    void updateDeviceState(Long id, Integer state);

    /**
     * 更新设备分组
     *
     * @param updateReqVO 更新信息
     */
    void updateDeviceGroup(@Valid IotDeviceUpdateGroupReqVO updateReqVO);

    /**
     * 删除单个设备
     *
     * @param id 编号
     */
    void deleteDevice(Long id);

    /**
     * 删除多个设备
     *
     * @param ids 编号数组
     */
    void deleteDeviceList(Collection<Long> ids);

    /**
     * 校验设备是否存在
     *
     * @param id 设备 ID
     * @return 设备对象
     */
    IotDeviceDO validateDeviceExists(Long id);

    /**
     * 获得设备
     *
     * @param id 编号
     * @return IoT 设备
     */
    IotDeviceDO getDevice(Long id);

    /**
     * 根据设备 key 获得设备
     *
     * @param deviceKey 编号
     * @return IoT 设备
     */
    IotDeviceDO getDeviceByDeviceKey(String deviceKey);

    /**
     * 获得设备分页
     *
     * @param pageReqVO 分页查询
     * @return IoT 设备分页
     */
    PageResult<IotDeviceDO> getDevicePage(IotDevicePageReqVO pageReqVO);

    /**
     * 基于设备类型，获得设备列表
     *
     * @param deviceType 设备类型
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceListByDeviceType(@Nullable Integer deviceType);

    /**
     * 获得状态，获得设备列表
     *
     * @param state 状态
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceListByState(Integer state);

    /**
     * 根据产品ID获取设备列表
     *
     * @param productId 产品ID，用于查询特定产品的设备列表
     * @return 返回与指定产品ID关联的设备列表，列表中的每个元素为IotDeviceDO对象
     */
    List<IotDeviceDO> getDeviceListByProductId(Long productId);

    /**
     * 根据设备ID列表获取设备信息列表
     *
     * @param deviceIdList 设备ID列表，包含需要查询的设备ID
     * @return 返回与设备ID列表对应的设备信息列表，列表中的每个元素为IotDeviceDO对象
     */
    List<IotDeviceDO> getDeviceListByIdList(List<Long> deviceIdList);

    /**
     * 基于产品编号，获得设备数量
     *
     * @param productId 产品编号
     * @return 设备数量
     */
    Long getDeviceCountByProductId(Long productId);

    /**
     * 获得设备数量
     *
     * @param groupId 分组编号
     * @return 设备数量
     */
    Long getDeviceCountByGroupId(Long groupId);

    /**
     * 【缓存】根据产品 key 和设备名称，获得设备信息
     * <p>
     * 注意：该方法会忽略租户信息，所以调用时，需要确认会不会有跨租户访问的风险！！！
     *
     * @param productKey 产品 key
     * @param deviceName 设备名称
     * @return 设备信息
     */
    IotDeviceDO getDeviceByProductKeyAndDeviceNameFromCache(String productKey, String deviceName);

    /**
     * 导入设备
     *
     * @param importDevices 导入设备列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    IotDeviceImportRespVO importDevice(List<IotDeviceImportExcelVO> importDevices, boolean updateSupport);

    /**
     * 获得设备数量
     *
     * @param createTime 创建时间，如果为空，则统计所有设备数量
     * @return 设备数量
     */
    Long getDeviceCount(@Nullable LocalDateTime createTime);

    /**
     * 获取 MQTT 连接参数
     *
     * @param deviceId 设备 ID
     * @return MQTT 连接参数
     */
    IotDeviceMqttConnectionParamsRespVO getMqttConnectionParams(Long deviceId);

    /**
     * 获得各个产品下的设备数量 Map
     *
     * @return key: 产品编号, value: 设备数量
     */
    Map<Long, Integer> getDeviceCountMapByProductId();

    /**
     * 获得各个状态下的设备数量 Map
     *
     * @return key: 设备状态枚举 {@link IotDeviceStateEnum}
     *         value: 设备数量
     */
    Map<Integer, Long> getDeviceCountMapByState();

}