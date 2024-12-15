package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceStatusUpdateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceUpdateGroupReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceImportRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.device.IotDeviceImportExcelVO;
import jakarta.validation.Valid;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * IoT 设备 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceService {

    /**
     * 创建设备
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

    /**
     * 更新设备状态
     *
     * @param updateReqVO 更新信息
     */
    void updateDeviceStatus(IotDeviceStatusUpdateReqVO updateReqVO);

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
     * 获得设备
     *
     * @param id 编号
     * @return IoT 设备
     */
    IotDeviceDO getDevice(Long id);

    /**
     * ��得设备分页
     *
     * @param pageReqVO 分页查询
     * @return IoT 设备分页
     */
    PageResult<IotDeviceDO> getDevicePage(IotDevicePageReqVO pageReqVO);

    /**
     * 获得设备列表
     *
     * @param deviceType 设备类型
     * @return 设备列表
     */
    List<IotDeviceDO> getDeviceList(@Nullable Integer deviceType);

    /**
     * 获得设备数量
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
     * 根据产品 key 和设备名称，获得设备信息
     *
     * @param productKey 产品 key
     * @param deviceName 设备名称
     * @return 设备信息
     */
    IotDeviceDO getDeviceByProductKeyAndDeviceName(String productKey, String deviceName);

    /**
     * 导入设备
     *
     * @param importDevices 导入设备列表
     * @param updateSupport 是否支持更新
     * @return 导入结果
     */
    IotDeviceImportRespVO importDevice(List<IotDeviceImportExcelVO> importDevices, boolean updateSupport);

}