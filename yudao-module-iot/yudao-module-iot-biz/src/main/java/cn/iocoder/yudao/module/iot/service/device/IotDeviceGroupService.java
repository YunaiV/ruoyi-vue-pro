package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.group.IotDeviceGroupPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.group.IotDeviceGroupSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceGroupDO;
import jakarta.validation.Valid;

/**
 * IoT 设备分组 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceGroupService {

    /**
     * 创建IoT 设备分组
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDeviceGroup(@Valid IotDeviceGroupSaveReqVO createReqVO);

    /**
     * 更新IoT 设备分组
     *
     * @param updateReqVO 更新信息
     */
    void updateDeviceGroup(@Valid IotDeviceGroupSaveReqVO updateReqVO);

    /**
     * 删除IoT 设备分组
     *
     * @param id 编号
     */
    void deleteDeviceGroup(Long id);

    /**
     * 获得IoT 设备分组
     *
     * @param id 编号
     * @return IoT 设备分组
     */
    IotDeviceGroupDO getDeviceGroup(Long id);

    /**
     * 获得IoT 设备分组分页
     *
     * @param pageReqVO 分页查询
     * @return IoT 设备分组分页
     */
    PageResult<IotDeviceGroupDO> getDeviceGroupPage(IotDeviceGroupPageReqVO pageReqVO);

}