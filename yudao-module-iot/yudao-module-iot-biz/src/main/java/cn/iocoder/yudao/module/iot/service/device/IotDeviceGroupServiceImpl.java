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

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
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
        // 校验存在
        validateDeviceGroupExists(id);
        // 删除
        deviceGroupMapper.deleteById(id);
    }

    private void validateDeviceGroupExists(Long id) {
        if (deviceGroupMapper.selectById(id) == null) {
            throw exception(DEVICE_GROUP_NOT_EXISTS);
        }
    }

    @Override
    public IotDeviceGroupDO getDeviceGroup(Long id) {
        return deviceGroupMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceGroupDO> getDeviceGroupPage(IotDeviceGroupPageReqVO pageReqVO) {
        return deviceGroupMapper.selectPage(pageReqVO);
    }

}