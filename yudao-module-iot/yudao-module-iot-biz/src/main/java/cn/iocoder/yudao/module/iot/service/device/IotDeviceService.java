package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.*;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

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
     * @param device 设备信息
     * @param state 状态
     */
    void updateDeviceState(IotDeviceDO device, Integer state);

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
     * 【缓存】校验设备是否存在
     *
     * @param id 设备 ID
     * @return 设备对象
     */
    IotDeviceDO validateDeviceExistsFromCache(Long id);

    /**
     * 获得设备
     *
     * @param id 编号
     * @return IoT 设备
     */
    IotDeviceDO getDevice(Long id);

    /**
     * 【缓存】获得设备信息
     * <p>
     * 注意：该方法会忽略租户信息，所以调用时，需要确认会不会有跨租户访问的风险！！！
     *
     * @param id 编号
     * @return IoT 设备
     */
    IotDeviceDO getDeviceFromCache(Long id);

    /**
     * 【缓存】根据产品 key 和设备名称，获得设备信息
     * <p>
     * 注意：该方法会忽略租户信息，所以调用时，需要确认会不会有跨租户访问的风险！！！
     *
     * @param productKey 产品 key
     * @param deviceName 设备名称
     * @return 设备信息
     */
    IotDeviceDO getDeviceFromCache(String productKey, String deviceName);

    /**
     * 获得设备分页
     *
     * @param pageReqVO 分页查询
     * @return IoT 设备分页
     */
    PageResult<IotDeviceDO> getDevicePage(IotDevicePageReqVO pageReqVO);

    /**
     * 根据条件，获得设备列表
     *
     * @param deviceType 设备类型
     * @param productId 产品编号
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceListByCondition(@Nullable Integer deviceType,
                                               @Nullable Long productId);

    /**
     * 获得状态，获得设备列表
     *
     * @param state 状态
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceListByState(Integer state);

    /**
     * 根据产品编号，获取设备列表
     *
     * @param productId 产品编号
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceListByProductId(Long productId);

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
     * 获得设备认证信息
     *
     * @param id 设备编号
     * @return MQTT 连接参数
     */
    IotDeviceAuthInfoRespVO getDeviceAuthInfo(Long id);

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

    /**
     * 通过产品标识和设备名称列表获取设备列表
     *
     * @param productKey  产品标识
     * @param deviceNames 设备名称列表
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceListByProductKeyAndNames(String productKey, List<String> deviceNames);

    /**
     * 认证设备
     *
     * @param authReqDTO 认证信息
     * @return 是否认证成功
     */
    boolean authDevice(@Valid IotDeviceAuthReqDTO authReqDTO);

    /**
     * 校验设备是否存在
     *
     * @param ids 设备编号数组
     */
    List<IotDeviceDO> validateDeviceListExists(Collection<Long> ids);

    /**
     * 获得设备列表
     *
     * @param ids 设备编号数组
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceList(Collection<Long> ids);

    /**
     * 获得设备 Map
     *
     * @param ids 设备编号数组
     * @return 设备 Map
     */
    default Map<Long, IotDeviceDO> getDeviceMap(Collection<Long> ids) {
        return convertMap(getDeviceList(ids), IotDeviceDO::getId);
    }

    /**
     * 更新设备固件版本
     *
     * @param deviceId 设备编号
     * @param firmwareId 固件编号
     */
    void updateDeviceFirmware(Long deviceId, Long firmwareId);

}
