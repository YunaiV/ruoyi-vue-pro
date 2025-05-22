package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.group.IotDeviceGroupPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.group.IotDeviceGroupSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceGroupDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * IoT 设备分组 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceGroupService {

    /**
     * 创建设备分组
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDeviceGroup(@Valid IotDeviceGroupSaveReqVO createReqVO);

    /**
     * 更新设备分组
     *
     * @param updateReqVO 更新信息
     */
    void updateDeviceGroup(@Valid IotDeviceGroupSaveReqVO updateReqVO);

    /**
     * 删除设备分组
     *
     * @param id 编号
     */
    void deleteDeviceGroup(Long id);

    /**
     * 校验设备分组是否存在
     *
     * @param ids 设备分组 ID 数组
     */
    default List<IotDeviceGroupDO> validateDeviceGroupExists(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return convertList(ids, this::validateDeviceGroupExists);
    }

    /**
     * 校验设备分组是否存在
     *
     * @param id 设备分组 ID
     * @return 设备分组
     */
    IotDeviceGroupDO validateDeviceGroupExists(Long id);

    /**
     * 获得设备分组
     *
     * @param id 编号
     * @return 设备分组
     */
    IotDeviceGroupDO getDeviceGroup(Long id);

    /**
     * 获得设备分组
     *
     * @param name 名称
     * @return 设备分组
     */
    IotDeviceGroupDO getDeviceGroupByName(String name);

    /**
     * 获得设备分组分页
     *
     * @param pageReqVO 分页查询
     * @return 设备分组分页
     */
    PageResult<IotDeviceGroupDO> getDeviceGroupPage(IotDeviceGroupPageReqVO pageReqVO);

    /**
     * 获得设备分组列表
     *
     * @param status 状态
     * @return 设备分组列表
     */
    List<IotDeviceGroupDO> getDeviceGroupListByStatus(Integer status);

}