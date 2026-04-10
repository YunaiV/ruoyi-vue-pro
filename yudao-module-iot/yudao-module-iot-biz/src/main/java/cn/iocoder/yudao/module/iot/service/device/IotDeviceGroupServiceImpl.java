package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.group.IotDeviceGroupPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.group.IotDeviceGroupSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceGroupDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceGroupMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_GROUP_DELETE_FAIL_DEVICE_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_GROUP_NOT_EXISTS;

/**
 * IoT 设备分组 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotDeviceGroupServiceImpl implements IotDeviceGroupService {

    @Resource
    private IotDeviceGroupMapper deviceGroupMapper;

    @Resource
    private IotDeviceService deviceService;

    @Override
    public Long createDeviceGroup(IotDeviceGroupSaveReqVO createReqVO) {
        // 插入
        IotDeviceGroupDO deviceGroup = BeanUtils.toBean(createReqVO, IotDeviceGroupDO.class);
        deviceGroupMapper.insert(deviceGroup);
        // 返回
        return deviceGroup.getId();
    }

    @Override
    public void updateDeviceGroup(IotDeviceGroupSaveReqVO updateReqVO) {
        // 校验存在
        validateDeviceGroupExists(updateReqVO.getId());
        // 更新
        IotDeviceGroupDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceGroupDO.class);
        deviceGroupMapper.updateById(updateObj);
    }

    @Override
    public void deleteDeviceGroup(Long id) {
        // 1.1 校验存在
        validateDeviceGroupExists(id);
        // 1.2 校验是否存在设备
        if (deviceService.getDeviceCountByGroupId(id) > 0) {
            throw exception(DEVICE_GROUP_DELETE_FAIL_DEVICE_EXISTS);
        }

        // 删除
        deviceGroupMapper.deleteById(id);
    }

    @Override
    public IotDeviceGroupDO validateDeviceGroupExists(Long id) {
        IotDeviceGroupDO group = deviceGroupMapper.selectById(id);
        if (group == null) {
            throw exception(DEVICE_GROUP_NOT_EXISTS);
        }
        return group;
    }

    @Override
    public IotDeviceGroupDO getDeviceGroup(Long id) {
        return deviceGroupMapper.selectById(id);
    }

    @Override
    public IotDeviceGroupDO getDeviceGroupByName(String name) {
        return deviceGroupMapper.selectByName(name);
    }

    @Override
    public PageResult<IotDeviceGroupDO> getDeviceGroupPage(IotDeviceGroupPageReqVO pageReqVO) {
        return deviceGroupMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotDeviceGroupDO> getDeviceGroupListByStatus(Integer status) {
        return deviceGroupMapper.selectListByStatus(status);
    }

}